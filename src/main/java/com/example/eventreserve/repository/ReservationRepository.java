package com.example.eventreserve.repository;

import com.example.eventreserve.entity.Reservation;
import com.example.eventreserve.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** 状態ごとの人数（予約確定の人数、キャンセル待ちの人数） */
    long countByEventIdAndStatus(Long eventId, ReservationStatus status);

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    Optional<Reservation> findByEventIdAndMemberId(Long eventId, Long memberId);

    /** イベントの予約を全部（削除のとき用） */
    List<Reservation> findByEventIdOrderByIdAsc(Long eventId);

    /** イベントの予約を状態ごとに、申し込み順で */
    List<Reservation> findByEventIdAndStatusOrderByIdAsc(Long eventId, ReservationStatus status);

    /** キャンセル待ちの先頭（いちばん早く申し込んだ人） */
    Optional<Reservation> findFirstByEventIdAndStatusOrderByIdAsc(Long eventId, ReservationStatus status);

    /** 自分より前に申し込んだキャンセル待ちの人数（順番の計算用） */
    long countByEventIdAndStatusAndIdLessThan(Long eventId, ReservationStatus status, Long id);

    /** マイページ用：自分の予約をイベントの開催日時順に */
    List<Reservation> findByMemberIdOrderByEventStartAtAsc(Long memberId);
}
