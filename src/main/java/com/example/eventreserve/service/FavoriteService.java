package com.example.eventreserve.service;

import com.example.eventreserve.entity.Event;
import com.example.eventreserve.entity.Favorite;
import com.example.eventreserve.repository.EventRepository;
import com.example.eventreserve.repository.FavoriteRepository;
import com.example.eventreserve.repository.MemberRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** お気に入り（検討リスト） */
@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, EventRepository eventRepository,
                           MemberRepository memberRepository) {
        this.favoriteRepository = favoriteRepository;
        this.eventRepository = eventRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * お気に入りに入っていなければ追加、入っていれば外す。
     * @return 追加したら true、外したら false
     */
    @Transactional
    public boolean toggle(Long eventId, Long memberId) {
        Optional<Favorite> existing = favoriteRepository.findByEventIdAndMemberId(eventId, memberId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false;
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException("イベントが見つかりません"));
        try {
            favoriteRepository.saveAndFlush(new Favorite(event, memberRepository.getReferenceById(memberId)));
        } catch (DataIntegrityViolationException e) {
            // 2回続けて押されたときなど。すでに入っているので問題なし
        }
        return true;
    }

    /** マイページ用: 自分のお気に入り（予約期限の近い順） */
    @Transactional(readOnly = true)
    public List<Event> listMine(Long memberId) {
        return favoriteRepository.findByMemberIdOrderByEventDeadlineAsc(memberId).stream()
                .map(Favorite::getEvent)
                .toList();
    }
}
