package com.devsehyunjin.account.domain;

import com.devsehyunjin.account.domain.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long balance;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    private LocalDateTime createdAt;

    private LocalDateTime closedAt;

    private AccountStatus status;

    // 계좌가 해지된 상태인지 확인
    public boolean isClosed() {
        return this.status == AccountStatus.CLOSED;
    }

    // 필수 필드들만 생성자에서 초기화
    @Builder
    private Account(String accountNumber, User user, Long initialBalance) {
        this.accountNumber = accountNumber;
        this.user = user;
        this.balance = initialBalance;
        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
        this.status = AccountStatus.ACTIVE; // 계좌 생성 시 초기 상태 설정
    }

    // 정적 팩토리 메서드 추가 (계좌 생성 시 사용하는 함수)
    public static Account createAccount(String accountNumber, User user, Long initialBalance) {
        return new Account(accountNumber, user, initialBalance);
    }

    // 계좌 해지 비즈니스 메서드
    public void closeAccount() {
        this.isDeleted = true;
        this.closedAt = LocalDateTime.now();
        this.status = AccountStatus.CLOSED; // 계좌 상태를 CLOSED로 변경
    }

    public void updateBalanceForUse(Long amount) {
        this.balance -= amount;
    }

    public void updateBalanceForCancel(Long amount) {
        if (this.isDeleted) {
            throw new IllegalStateException("해지된 계좌에는 잔액을 변경할 수 없습니다.");
        }
        if (this.balance + amount < 0) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        this.balance += amount;
    }
}
