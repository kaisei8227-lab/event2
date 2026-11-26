package com.example.eventreserve.service;

import com.example.eventreserve.entity.Event;
import com.example.eventreserve.entity.Reservation;

import java.time.LocalDateTime;

/** マイページに表示する1件分 */
public class MyReservationView {

    private final Reservation reservation;
    private final long waitingPosition;

    public MyReservationView(Reservation reservation, long waitingPosition) {
        this.reservation = reservation;
        this.waitingPosition = waitingPosition;
    }

    public Reservation getReservation() { return reservation; }
    public Event getEvent() { return reservation.getEvent(); }

    /** キャンセル待ちの何番目か（予約確定なら 0） */
    public long getWaitingPosition() { return waitingPosition; }

    /** 予約期限までならキャンセルできる */
    public boolean isCancellable() {
        return !getEvent().isClosed();
    }

    /** イベントがもう終わっているか（開催日時を過ぎた） */
    public boolean isPast() {
        return getEvent().getStartAt().isBefore(LocalDateTime.now());
    }
}
