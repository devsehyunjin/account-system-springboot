package com.devsehyunjin.account.dto;

import lombok.Getter;

@Getter
public class CancelTransactionRequest {
    private Long transactionId;
    private String accountNumber;
    private Long amount;
}
