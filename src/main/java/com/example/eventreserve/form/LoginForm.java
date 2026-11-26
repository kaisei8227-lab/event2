package com.example.eventreserve.form;

import com.example.eventreserve.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class LoginForm {

    /** 学生ログインか企業ログインか（最初は学生を選んだ状態） */
    @NotNull(message = "学生か企業かを選んでください")
    private Role role = Role.USER;

    @NotBlank(message = "メールアドレスを入力してください")
    @Pattern(regexp = ValidationRules.EMAIL, message = "メールアドレスの形が正しくありません")
    private String email;

    @NotBlank(message = "パスワードを入力してください")
    private String password;

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
