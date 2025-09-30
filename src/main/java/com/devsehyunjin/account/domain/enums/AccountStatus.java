package com.devsehyunjin.account.domain.enums;

import lombok.Getter;

@Getter
public enum AccountStatus {
    ACTIVE("활성화된 계좌"),
    CLOSED("계좌 해지됨");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

}