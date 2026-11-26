package com.example.eventreserve.repository;

import com.example.eventreserve.entity.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    /** 掲示板用: 予約期限がまだ来ていないイベントだけを、開催日時の早い順に */
    List<Event> findByDeadlineAfterOrderByStartAtAsc(LocalDateTime now);

    /** 管理画面用: 自分の会社が配信したイベントだけ */
    List<Event> findByCreatedByIdOrderByStartAtAsc(Long memberId);

    /**
     * 予約するときに使う。行ロックをかけて読み込むので、
     * 同時に2人が最後の1席を予約しても定員を超えない。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}
