package com.devsehyunjin.account.dto;

import lombok.Getter;

@Getter
public class AccountResponse {
    private String accountNumber;
    private Long balance;
    private String status;
}