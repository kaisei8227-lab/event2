package com.example.eventreserve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/** お気に入り（検討リスト）。学生が気になるイベントを保存しておく */
@Entity
@Table(name = "favorites",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "member_id"}))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Favorite() {
    }

    public Favorite(Event event, Member member) {
        this.event = event;
        this.member = member;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public Member getMember() { return member; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
