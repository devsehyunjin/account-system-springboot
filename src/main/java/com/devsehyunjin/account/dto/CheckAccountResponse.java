package com.devsehyunjin.account.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckAccountResponse {
    private String accountNumber;
    private Long balance;
}
