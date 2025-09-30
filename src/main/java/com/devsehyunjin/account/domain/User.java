package com.devsehyunjin.account.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 표준 스펙 준수를 위한 protected 기본 생성자
@Table(name = "users")
@Builder
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final List<Account> accounts = new ArrayList<>();

    // 필수 값만 초기화
    public User(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    // 계좌 등록 비즈니스 메서드
    public void addAccount(Account account) {
        if (this.accounts.size() >= 10) {
            throw new IllegalStateException("사용자는 최대 10개의 계좌만 가질 수 있습니다.");
        }
        this.accounts.add(account);
    }
}