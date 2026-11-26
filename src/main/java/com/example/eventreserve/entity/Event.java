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

import java.time.LocalDateTime;

/** 企業が作成して掲示板に載せるイベント */
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** イベント名 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 企業名（作成した企業アカウントの企業名をコピーして入れる） */
    @Column(nullable = false, length = 100)
    private String companyName;

    /** 何人まで参加できるか（定員） */
    @Column(nullable = false)
    private int capacity;

    /** イベント説明 */
    @Column(length = 1000)
    private String description;

    /** 会場 */
    @Column(nullable = false, length = 100)
    private String location;

    /** イベント日時 */
    @Column(nullable = false)
    private LocalDateTime startAt;

    /** 予約期限（この日時を過ぎると予約できない） */
    @Column(nullable = false)
    private LocalDateTime deadline;

    /** 作成した企業アカウント */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private Member createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Event() {
    }

    public Event(String title, String companyName, int capacity, String description, String location,
                 LocalDateTime startAt, LocalDateTime deadline, Member createdBy) {
        this.title = title;
        this.companyName = companyName;
        this.capacity = capacity;
        this.description = description;
        this.location = location;
        this.startAt = startAt;
        this.deadline = deadline;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    /** 企業がイベントの内容を編集する */
    public void update(String title, int capacity, String description, String location,
                       LocalDateTime startAt, LocalDateTime deadline) {
        this.title = title;
        this.capacity = capacity;
        this.description = description;
        this.location = location;
        this.startAt = startAt;
        this.deadline = deadline;
    }

    /** 企業名が変わったときに、掲示中のイベントの表示もそろえる */
    public void changeCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /** 予約期限まで48時間を切っているか（まだ締め切っていないもの） */
    public boolean isDeadlineSoon() {
        LocalDateTime now = LocalDateTime.now();
        return !isClosed() && deadline.isBefore(now.plusHours(48));
    }

    /** 予約期限を過ぎていたら受付終了 */
    public boolean isClosed() {
        return LocalDateTime.now().isAfter(deadline);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCompanyName() { return companyName; }
    public int getCapacity() { return capacity; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getDeadline() { return deadline; }
    public Member getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
