package com.devsehyunjin.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CancelTransactionRequest {
    private Long transactionId;
    private String accountNumber;
    private Long amount;
}
