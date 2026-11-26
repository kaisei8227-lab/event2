package com.example.eventreserve.form;

import com.example.eventreserve.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterForm {

    /** 学生として登録するか企業として登録するか */
    @NotNull(message = "学生か企業かを選んでください")
    private Role role = Role.USER;

    /** 企業名（企業を選んだときだけ必須。チェックは AuthController で行う） */
    @Size(max = 100, message = "企業名は100文字以内にしてください")
    private String companyName;

    @NotBlank(message = "メールアドレスを入力してください")
    @Pattern(regexp = ValidationRules.EMAIL, message = "メールアドレスの形が正しくありません")
    private String email;

    @NotBlank(message = "パスワードを入力してください")
    @Pattern(regexp = ValidationRules.PASSWORD, message = "パスワードは英字と数字をふくむ8文字以上にしてください")
    private String password;

    @NotBlank(message = "確認用パスワードを入力してください")
    private String passwordConfirm;

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPasswordConfirm() { return passwordConfirm; }
    public void setPasswordConfirm(String passwordConfirm) { this.passwordConfirm = passwordConfirm; }
}
