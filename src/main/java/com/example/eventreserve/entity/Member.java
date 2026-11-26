package com.example.eventreserve.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 会員（学生 or 企業）。メールアドレスとパスワードを保管する。
 * パスワードはそのまま保存せず、BCryptでハッシュ化した文字列を保存する。
 */
@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** ハッシュ化済みのパスワード（元のパスワードには戻せない） */
    @Column(nullable = false, length = 100)
    private String passwordHash;

    /** 学生（USER）か企業（COMPANY）か */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** 企業名（企業アカウントのときだけ入る。掲示板に表示される） */
    @Column(length = 100)
    private String companyName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Member() {
    }

    public Member(String email, String passwordHash, Role role, String companyName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.companyName = companyName;
        this.createdAt = LocalDateTime.now();
    }

    /** パスワードを変える（ハッシュ化したものを受け取る） */
    public void changePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /** 企業名を変える */
    public void changeCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public boolean isCompany() {
        return role == Role.COMPANY;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public String getCompanyName() { return companyName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
