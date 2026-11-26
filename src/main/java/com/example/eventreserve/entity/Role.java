package com.example.eventreserve.entity;

/** 会員の種類 */
public enum Role {
    /** 学生: イベントを予約できる */
    USER("学生"),
    /** 企業: イベントを作成して掲示板に載せられる */
    COMPANY("企業");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    /** 画面に出す名前 */
    public String getLabel() {
        return label;
    }
}
