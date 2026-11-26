package com.example.eventreserve.repository;

import com.example.eventreserve.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByEventIdAndMemberId(Long eventId, Long memberId);

    Optional<Favorite> findByEventIdAndMemberId(Long eventId, Long memberId);

    /** マイページ用: 予約期限の近い順 */
    List<Favorite> findByMemberIdOrderByEventDeadlineAsc(Long memberId);

    /** イベント削除のとき用 */
    List<Favorite> findByEventId(Long eventId);
}
