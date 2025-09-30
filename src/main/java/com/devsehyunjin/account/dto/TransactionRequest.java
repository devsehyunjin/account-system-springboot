package com.devsehyunjin.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private Long userId;
    private String accountNumber;
    private Long amount;
}
