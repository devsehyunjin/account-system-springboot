package com.devsehyunjin.account.domain.enums;

import lombok.Getter;

@Getter
public enum TransactionResult {

    SUCCESS("거래 성공"),  // 거래 성공
    FAILURE("거래 실패"); // 거래 실패

    private final String description;

    // 생성자를 통해 설명 초기화
    TransactionResult(String description) {
        this.description = description;
    }

}