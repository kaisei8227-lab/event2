package com.example.eventreserve.service;

import com.example.eventreserve.entity.Event;
import com.example.eventreserve.entity.Member;
import com.example.eventreserve.entity.Reservation;
import com.example.eventreserve.entity.ReservationStatus;
import com.example.eventreserve.form.EventForm;
import com.example.eventreserve.repository.EventRepository;
import com.example.eventreserve.repository.FavoriteRepository;
import com.example.eventreserve.repository.MemberRepository;
import com.example.eventreserve.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationService reservationService;
    private final FavoriteRepository favoriteRepository;

    public EventService(EventRepository eventRepository,
                        ReservationRepository reservationRepository,
                        MemberRepository memberRepository,
                        ReservationService reservationService,
                        FavoriteRepository favoriteRepository) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.reservationService = reservationService;
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * 掲示板（イベント一覧）用。
     * 予約期限を過ぎたイベントは表示しない（開くたびに今の時刻で絞り込む）。
     * さらに検索条件で絞り込む。
     */
    @Transactional(readOnly = true)
    public List<EventView> listForMember(Long memberId, EventSearch search) {
        String keyword = (search.getQ() == null) ? "" : search.getQ().trim().toLowerCase(Locale.ROOT);
        return eventRepository.findByDeadlineAfterOrderByStartAtAsc(LocalDateTime.now()).stream()
                .filter(e -> keyword.isEmpty() || matches(e, keyword))
                .filter(e -> search.getFrom() == null || !e.getStartAt().toLocalDate().isBefore(search.getFrom()))
                .filter(e -> search.getTo() == null || !e.getStartAt().toLocalDate().isAfter(search.getTo()))
                .map(e -> toView(e, memberId))
                .filter(v -> !search.isAvailable() || !v.isFull())
                .toList();
    }

    /** キーワードがイベント名・企業名・会場・説明のどれかにふくまれるか（大文字小文字は区別しない） */
    private boolean matches(Event e, String keyword) {
        return contains(e.getTitle(), keyword)
                || contains(e.getCompanyName(), keyword)
                || contains(e.getLocation(), keyword)
                || contains(e.getDescription(), keyword);
    }

    private boolean contains(String text, String keyword) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(keyword);
    }

    /** 企業の管理画面用: 自社のイベントだけ */
    @Transactional(readOnly = true)
    public List<EventView> listForCompany(Long companyMemberId) {
        return eventRepository.findByCreatedByIdOrderByStartAtAsc(companyMemberId).stream()
                .map(e -> toView(e, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public EventView getView(Long eventId, Long memberId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("イベントが見つかりません"));
        return toView(event, memberId);
    }

    /** 企業がイベントを作成して掲示板に追加する */
    @Transactional
    public Event publish(EventForm form, Long companyMemberId) {
        Member company = memberRepository.findById(companyMemberId)
                .orElseThrow(() -> new BusinessException("企業アカウントが見つかりません"));
        Event event = new Event(
                form.getTitle().trim(),
                company.getCompanyName(),   // 企業名はアカウントのものを使う
                form.getCapacity(),
                blankToNull(form.getDescription()),
                form.getLocation().trim(),
                form.getStartAt(),
                form.getDeadline(),
                company);
        return eventRepository.save(event);
    }

    /**
     * 企業がイベントを編集する。
     * ・参加人数は、すでに予約が確定している人数より少なくできない
     * ・参加人数を増やしたら、増えた分だけキャンセル待ちの人を自動で繰り上げる
     *
     * @return 繰り上がった人数
     */
    @Transactional
    public int update(Long eventId, Long companyMemberId, EventForm form) {
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new BusinessException("イベントが見つかりません"));
        checkOwner(event, companyMemberId);

        long confirmed = reservationRepository.countByEventIdAndStatus(eventId, ReservationStatus.CONFIRMED);
        if (form.getCapacity() < confirmed) {
            throw new BusinessException("参加人数は、すでに予約が確定している人数（" + confirmed + "人）より少なくできません");
        }

        event.update(
                form.getTitle().trim(),
                form.getCapacity(),
                blankToNull(form.getDescription()),
                form.getLocation().trim(),
                form.getStartAt(),
                form.getDeadline());
        return reservationService.promoteWaiting(event);
    }

    /** 企業がイベントを取り下げる（予約・キャンセル待ちもいっしょに消える） */
    @Transactional
    public void delete(Long eventId, Long companyMemberId) {
        Event event = findOwnEvent(eventId, companyMemberId);
        reservationRepository.deleteAll(reservationRepository.findByEventIdOrderByIdAsc(eventId));
        favoriteRepository.deleteAll(favoriteRepository.findByEventId(eventId));
        eventRepository.delete(event);
    }

    /** 企業が自社イベントの予約確定者を見る（申し込み順） */
    @Transactional(readOnly = true)
    public List<Reservation> confirmedOf(Long eventId, Long companyMemberId) {
        findOwnEvent(eventId, companyMemberId);
        return reservationRepository.findByEventIdAndStatusOrderByIdAsc(eventId, ReservationStatus.CONFIRMED);
    }

    /** 企業が自社イベントのキャンセル待ちを見る（順番どおり） */
    @Transactional(readOnly = true)
    public List<Reservation> waitingOf(Long eventId, Long companyMemberId) {
        findOwnEvent(eventId, companyMemberId);
        return reservationRepository.findByEventIdAndStatusOrderByIdAsc(eventId, ReservationStatus.WAITING);
    }

    /**
     * 企業が予約者の出欠を記録する。
     * @param attended true=出席 / false=欠席 / null=未確認に戻す
     */
    @Transactional
    public Reservation markAttendance(Long reservationId, Long companyMemberId, Boolean attended) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("予約が見つかりません"));
        checkOwner(r.getEvent(), companyMemberId);
        if (!r.isConfirmed()) {
            throw new BusinessException("キャンセル待ちの人は出欠を記録できません");
        }
        r.markAttendance(attended);
        return r;
    }

    @Transactional(readOnly = true)
    public Event findOwnEvent(Long eventId, Long companyMemberId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("イベントが見つかりません"));
        checkOwner(event, companyMemberId);
        return event;
    }

    private void checkOwner(Event event, Long companyMemberId) {
        if (!event.getCreatedBy().getId().equals(companyMemberId)) {
            throw new BusinessException("このイベントを操作する権限がありません");
        }
    }

    private EventView toView(Event e, Long memberId) {
        long confirmed = reservationRepository.countByEventIdAndStatus(e.getId(), ReservationStatus.CONFIRMED);
        long waiting = reservationRepository.countByEventIdAndStatus(e.getId(), ReservationStatus.WAITING);
        ReservationStatus mine = (memberId == null) ? null
                : reservationRepository.findByEventIdAndMemberId(e.getId(), memberId)
                        .map(Reservation::getStatus).orElse(null);
        boolean favorite = memberId != null && favoriteRepository.existsByEventIdAndMemberId(e.getId(), memberId);
        return new EventView(e, confirmed, waiting, mine, favorite);
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
