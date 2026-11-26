package com.example.eventreserve.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 予約ページで入力する個人情報 */
public class ReservationForm {

    @NotBlank(message = "氏名を入力してください")
    @Size(max = 50, message = "氏名は50文字以内にしてください")
    private String fullName;

    @NotBlank(message = "フリガナを入力してください")
    @Pattern(regexp = ValidationRules.KANA, message = "フリガナは全角カタカナで入力してください")
    @Size(max = 50, message = "フリガナは50文字以内にしてください")
    private String fullNameKana;

    @NotBlank(message = "電話番号を入力してください")
    @Pattern(regexp = ValidationRules.PHONE, message = "電話番号の形が正しくありません（例: 090-1234-5678）")
    private String phone;

    @Size(max = 100, message = "学校名は100文字以内にしてください")
    private String school;

    @Size(max = 500, message = "備考は500文字以内にしてください")
    private String note;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getFullNameKana() { return fullNameKana; }
    public void setFullNameKana(String fullNameKana) { this.fullNameKana = fullNameKana; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSchool() { return school; }
    public void setSchool(String school) { this.school = school; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
