package com.example.eventreserve.entity;

/** 予約の状態 */
public enum ReservationStatus {
    /** 参加が確定している（定員の内側） */
    CONFIRMED("予約確定"),
    /** 満席のためキャンセル待ち。空きが出たら先着順で自動的に CONFIRMED になる */
    WAITING("キャンセル待ち");

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
