package com.devsehyunjin.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreateAccountResponse {
    private Long userId;
    private String accountNumber;
    private LocalDateTime registeredAt;
}