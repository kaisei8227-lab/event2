package com.example.eventreserve.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 企業がイベントを作成するときの入力。
 * 日時の前後関係（予約期限 ≦ イベント日時 など）は AdminController でチェックする。
 */
public class EventForm {

    @NotBlank(message = "イベント名を入力してください")
    @Size(max = 100, message = "イベント名は100文字以内にしてください")
    private String title;

    @NotNull(message = "何人まで参加できるかを入力してください")
    @Min(value = 1, message = "参加人数は1人以上にしてください")
    @Max(value = 1000, message = "参加人数は1000人以下にしてください")
    private Integer capacity;

    @Size(max = 1000, message = "イベント説明は1000文字以内にしてください")
    private String description;

    @NotBlank(message = "会場を入力してください")
    @Size(max = 100, message = "会場は100文字以内にしてください")
    private String location;

    /** <input type="datetime-local"> は「2026-12-10T13:00」の形で送られてくる */
    @NotNull(message = "イベント日時を入力してください")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startAt;

    @NotNull(message = "予約期限を入力してください")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime deadline;

    /** 編集画面を開くとき、今のイベントの内容をフォームに入れる */
    public static EventForm from(com.example.eventreserve.entity.Event e) {
        EventForm f = new EventForm();
        f.title = e.getTitle();
        f.capacity = e.getCapacity();
        f.description = e.getDescription();
        f.location = e.getLocation();
        f.startAt = e.getStartAt();
        f.deadline = e.getDeadline();
        return f;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}
