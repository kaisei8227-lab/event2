package com.example.eventreserve.service;

import com.example.eventreserve.entity.Event;
import com.example.eventreserve.entity.ReservationStatus;

/** 掲示板・管理画面に表示する1件分（イベント＋人数＋自分の予約状態） */
public class EventView {

    private final Event event;
    private final long reservedCount;
    private final long waitingCount;
    /** 自分の予約状態（予約していなければ null） */
    private final ReservationStatus myStatus;
    /** 自分のお気に入りに入っているか */
    private final boolean favorite;

    public EventView(Event event, long reservedCount, long waitingCount, ReservationStatus myStatus,
                     boolean favorite) {
        this.event = event;
        this.reservedCount = reservedCount;
        this.waitingCount = waitingCount;
        this.myStatus = myStatus;
        this.favorite = favorite;
    }

    public boolean isFavorite() { return favorite; }

    public Event getEvent() { return event; }
    /** 予約確定の人数 */
    public long getReservedCount() { return reservedCount; }
    /** キャンセル待ちの人数 */
    public long getWaitingCount() { return waitingCount; }
    public ReservationStatus getMyStatus() { return myStatus; }

    public long getRemaining() {
        return Math.max(0, event.getCapacity() - reservedCount);
    }

    public boolean isFull() {
        return getRemaining() == 0;
    }

    /** 予約済み（確定）か */
    public boolean isReservedByMe() {
        return myStatus == ReservationStatus.CONFIRMED;
    }

    /** キャンセル待ち中か */
    public boolean isWaitingByMe() {
        return myStatus == ReservationStatus.WAITING;
    }

    /** 予約ボタンを押せるか（空きあり） */
    public boolean isReservable() {
        return !isFull() && myStatus == null && !event.isClosed();
    }

    /** キャンセル待ちに登録できるか（満席） */
    public boolean isWaitlistable() {
        return isFull() && myStatus == null && !event.isClosed();
    }
}
