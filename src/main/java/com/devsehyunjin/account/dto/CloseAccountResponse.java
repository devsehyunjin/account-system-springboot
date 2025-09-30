package com.devsehyunjin.account.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class CloseAccountResponse {
    private Long userId;
    private String accountNumber;
    private LocalDateTime closedAt;
}
