package com.example.eventreserve.service;

import com.example.eventreserve.entity.Event;
import com.example.eventreserve.entity.Member;
import com.example.eventreserve.entity.Reservation;
import com.example.eventreserve.entity.ReservationStatus;
import com.example.eventreserve.form.ReservationForm;
import com.example.eventreserve.repository.EventRepository;
import com.example.eventreserve.repository.MemberRepository;
import com.example.eventreserve.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 予約・キャンセル・キャンセル待ちの繰り上げ。
 *
 * 人数にかかわる処理は、どれもイベントの行をロック（findByIdForUpdate）してから行う。
 * 同時にたくさん押されても1件ずつ順番に処理されるので、定員を超えない。
 */
@Service
public class ReservationService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public ReservationService(EventRepository eventRepository,
                              ReservationRepository reservationRepository,
                              MemberRepository memberRepository) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * 予約する。空きがあれば「予約確定」、満席なら「キャンセル待ち」として登録する。
     */
    @Transactional
    public Reservation reserve(Long eventId, Long memberId, ReservationForm form) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new BusinessException("イベントが見つかりません"));

        if (event.isClosed()) {
            throw new BusinessException("予約期限を過ぎたため予約できません");
        }
        if (reservationRepository.existsByEventIdAndMemberId(eventId, memberId)) {
            throw new BusinessException("このイベントはすでに予約（またはキャンセル待ち）しています");
        }

        long confirmed = reservationRepository.countByEventIdAndStatus(eventId, ReservationStatus.CONFIRMED);
        ReservationStatus status = (confirmed < event.getCapacity())
                ? ReservationStatus.CONFIRMED
                : ReservationStatus.WAITING;

        Member member = memberRepository.getReferenceById(memberId);
        Reservation reservation = new Reservation(
                event,
                member,
                form.getFullName().trim(),
                form.getFullNameKana().trim(),
                form.getPhone().trim(),
                blankToNull(form.getSchool()),
                blankToNull(form.getNote()),
                status);
        return reservationRepository.save(reservation);
    }

    /**
     * 予約（またはキャンセル待ち）を取り消す。
     * 予約確定の人がキャンセルしたら、キャンセル待ちの先頭の人を自動で繰り上げる。
     */
    @Transactional
    public CancelResult cancel(Long reservationId, Long memberId) {
        Reservation reservation = getOwn(reservationId, memberId);
        Event event = eventRepository.findByIdForUpdate(reservation.getEvent().getId())
                .orElseThrow(() -> new BusinessException("イベントが見つかりません"));

        if (event.isClosed()) {
            throw new BusinessException("予約期限を過ぎたためキャンセルできません。主催企業に直接ご連絡ください");
        }

        boolean wasConfirmed = reservation.isConfirmed();
        String title = event.getTitle();
        reservationRepository.delete(reservation);
        reservationRepository.flush(); // 消したことをDBに反映してから人数を数える

        if (wasConfirmed) {
            promoteWaiting(event);
        }
        return new CancelResult(title, wasConfirmed);
    }

    /**
     * 空いている席の数だけ、キャンセル待ちを申し込み順に予約確定へ繰り上げる。
     * キャンセルのときと、企業が参加人数を増やしたときに呼ぶ。
     * 呼び出す側でイベントの行をロックしておくこと。
     */
    @Transactional
    public int promoteWaiting(Event event) {
        long confirmed = reservationRepository.countByEventIdAndStatus(event.getId(), ReservationStatus.CONFIRMED);
        int promoted = 0;
        while (confirmed < event.getCapacity()) {
            Optional<Reservation> next = reservationRepository
                    .findFirstByEventIdAndStatusOrderByIdAsc(event.getId(), ReservationStatus.WAITING);
            if (next.isEmpty()) {
                break;
            }
            next.get().promote();
            reservationRepository.flush();
            confirmed++;
            promoted++;
        }
        return promoted;
    }

    /** キャンセル待ちの何番目か（1から数える）。予約確定なら 0 */
    @Transactional(readOnly = true)
    public long waitingPosition(Reservation r) {
        if (!r.isWaiting()) {
            return 0;
        }
        return reservationRepository.countByEventIdAndStatusAndIdLessThan(
                r.getEvent().getId(), ReservationStatus.WAITING, r.getId()) + 1;
    }

    /** マイページ用：自分の予約（キャンセル待ちもふくむ） */
    @Transactional(readOnly = true)
    public List<MyReservationView> listMine(Long memberId) {
        return reservationRepository.findByMemberIdOrderByEventStartAtAsc(memberId).stream()
                .map(r -> new MyReservationView(r, waitingPosition(r)))
                .toList();
    }

    /** 本人の予約でなければ見せない */
    @Transactional(readOnly = true)
    public Reservation getOwn(Long reservationId, Long memberId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("予約が見つかりません"));
        if (!r.getMember().getId().equals(memberId)) {
            throw new BusinessException("予約が見つかりません");
        }
        return r;
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    /** キャンセルの結果（画面のメッセージ用） */
    public record CancelResult(String eventTitle, boolean wasConfirmed) {
    }
}
