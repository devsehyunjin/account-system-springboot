package com.devsehyunjin.account.dto;

import com.devsehyunjin.account.domain.Transaction;
import com.devsehyunjin.account.domain.enums.TransactionResult;
import com.devsehyunjin.account.domain.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionResponse {
    private String accountNumber;
    private TransactionResult transactionResult;
    private Long transactionId;
    private Long amount;
    private LocalDateTime transactionDate;
    private TransactionType transactionType;

    public static TransactionResponse from(Transaction transaction) {
        return TransactionResponse.builder()
                .accountNumber(transaction.getAccount().getAccountNumber())
                .transactionResult(transaction.getTransactionResult())
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .transactionDate(transaction.getTransactionDate())
                .build();
    }

    public static TransactionResponse fromCheck(Transaction transaction) {
        return TransactionResponse.builder()
                .accountNumber(transaction.getAccount().getAccountNumber())
                .transactionResult(transaction.getTransactionResult())
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .transactionDate(transaction.getTransactionDate())
                .transactionType(transaction.getTransactionType())
                .build();
    }

}
