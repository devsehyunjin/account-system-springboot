package com.devsehyunjin.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CloseAccountRequest {
    private Long userId;
    private String accountNumber;
}
