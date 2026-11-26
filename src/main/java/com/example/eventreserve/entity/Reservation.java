package com.example.eventreserve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

/**
 * 予約（キャンセル待ちもふくむ）。予約ページで入力した個人情報もここに保存する。
 * 同じ会員が同じイベントを2回予約できないよう、DBでも一意制約をかける。
 */
@Entity
@Table(name = "reservations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "member_id"}))
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, length = 50)
    private String fullName;

    @Column(nullable = false, length = 50)
    private String fullNameKana;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 100)
    private String school;

    @Column(length = 500)
    private String note;

    /**
     * 予約確定 か キャンセル待ち か。
     * 前の版で作った予約が残っていても動くよう、DBの初期値を CONFIRMED にしている。
     */
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'CONFIRMED'")
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    /** キャンセル待ちから繰り上がった日時（繰り上がっていなければ null） */
    private LocalDateTime promotedAt;

    /** 出欠（企業が当日チェックする）。true=出席 / false=欠席 / null=まだ確認していない */
    private Boolean attended;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Reservation() {
    }

    public Reservation(Event event, Member member, String fullName, String fullNameKana,
                       String phone, String school, String note, ReservationStatus status) {
        this.event = event;
        this.member = member;
        this.fullName = fullName;
        this.fullNameKana = fullNameKana;
        this.phone = phone;
        this.school = school;
        this.note = note;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    /** キャンセル待ちから予約確定に繰り上げる */
    public void promote() {
        this.status = ReservationStatus.CONFIRMED;
        this.promotedAt = LocalDateTime.now();
    }

    /** 出欠を記録する（null で未確認に戻す） */
    public void markAttendance(Boolean attended) {
        this.attended = attended;
    }

    public boolean isConfirmed() {
        return status == ReservationStatus.CONFIRMED;
    }

    public boolean isWaiting() {
        return status == ReservationStatus.WAITING;
    }

    /** キャンセル待ちから繰り上がって確定したか */
    public boolean isPromoted() {
        return promotedAt != null;
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public Member getMember() { return member; }
    public String getFullName() { return fullName; }
    public String getFullNameKana() { return fullNameKana; }
    public String getPhone() { return phone; }
    public String getSchool() { return school; }
    public String getNote() { return note; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getPromotedAt() { return promotedAt; }
    public Boolean getAttended() { return attended; }

    /** 出欠の表示用 */
    public String getAttendanceLabel() {
        if (attended == null) return "未確認";
        return attended ? "出席" : "欠席";
    }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
