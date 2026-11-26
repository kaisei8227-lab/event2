package com.example.eventreserve.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** パスワード変更の入力 */
public class PasswordChangeForm {

    @NotBlank(message = "今のパスワードを入力してください")
    private String currentPassword;

    @NotBlank(message = "新しいパスワードを入力してください")
    @Pattern(regexp = ValidationRules.PASSWORD, message = "パスワードは英字と数字をふくむ8文字以上にしてください")
    private String newPassword;

    @NotBlank(message = "確認用のパスワードを入力してください")
    private String newPasswordConfirm;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getNewPasswordConfirm() { return newPasswordConfirm; }
    public void setNewPasswordConfirm(String newPasswordConfirm) { this.newPasswordConfirm = newPasswordConfirm; }
}
