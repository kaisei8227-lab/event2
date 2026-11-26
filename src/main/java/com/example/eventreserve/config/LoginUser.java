package com.example.eventreserve.config;

import com.example.eventreserve.entity.Member;
import com.example.eventreserve.entity.Role;

import java.io.Serializable;

/** ログイン中の会員。セッションに入れておく（パスワードは入れない） */
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** セッションに保存するときの名前 */
    public static final String SESSION_KEY = "LOGIN_USER";

    private final Long id;
    private final String email;
    private final Role role;
    private final String companyName;

    public LoginUser(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.role = member.getRole();
        this.companyName = member.getCompanyName();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public String getCompanyName() { return companyName; }

    public boolean isCompany() {
        return role == Role.COMPANY;
    }

    /** ヘッダーに出す名前（企業は企業名、学生はメールアドレス） */
    public String getDisplayName() {
        return isCompany() && companyName != null ? companyName : email;
    }
}
