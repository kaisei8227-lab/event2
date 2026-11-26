package com.example.eventreserve.service;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 掲示板の検索条件。画面の検索フォーム（GET）からそのまま受け取る。
 * 何も入れなければ全件。
 */
public class EventSearch {

    /** キーワード（イベント名・企業名・会場・説明のどれかにふくまれていればヒット） */
    private String q;

    /** 開催日がこの日以降 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    /** 開催日がこの日以前 */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    /** 空きがあるイベントだけ */
    private boolean available;

    /** 何か条件が入っているか（「条件をクリア」リンクの表示用） */
    public boolean isFiltered() {
        return (q != null && !q.isBlank()) || from != null || to != null || available;
    }

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }
    public LocalDate getFrom() { return from; }
    public void setFrom(LocalDate from) { this.from = from; }
    public LocalDate getTo() { return to; }
    public void setTo(LocalDate to) { this.to = to; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
