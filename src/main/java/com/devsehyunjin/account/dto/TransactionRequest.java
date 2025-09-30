package com.devsehyunjin.account.dto;

import lombok.Getter;

@Getter
public class TransactionRequest {
    private Long userId;
    private String accountNumber;
    private Long amount;
}
