package com.devsehyunjin.account.controller;

import com.devsehyunjin.account.dto.*;
import com.devsehyunjin.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // 계좌 생성
    @PostMapping(value = "/createAccount", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateAccountResponse> createAccount(@RequestBody CreateAccountRequest request) {
        CreateAccountResponse response = accountService.createAccount(request);
        return ResponseEntity.ok(response);
    }

    // 계좌 해지
    @PostMapping(value = "/close", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CloseAccountResponse> closeAccount(@RequestBody CloseAccountRequest request) {
        CloseAccountResponse closeAccountResponse = accountService.closeAccount(request);
        return ResponseEntity.ok(closeAccountResponse);
    }

    // 특정 사용자 계좌 조회
    @GetMapping(value = "/user", produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CheckAccountResponse>> getUserAccounts(@RequestParam("userId") Long userId) {
        List<CheckAccountResponse> checkAccountsResponse = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(checkAccountsResponse);
    }

    // 잔액 사용
    @PostMapping("/use")
    public ResponseEntity<TransactionResponse> useBalance(@RequestBody @Valid TransactionRequest request) {
        TransactionResponse response = accountService.useBalance(request);
        return ResponseEntity.ok(response);
    }

    // 잔액 사용 취소
    @PostMapping("/cancel")
    public ResponseEntity<TransactionResponse> cancelBalance(@RequestBody @Valid CancelTransactionRequest request) {
        TransactionResponse response = accountService.cancelBalance(request);
        return ResponseEntity.ok(response);
    }

    // 거래 확인
    @GetMapping("/check")
    public ResponseEntity<TransactionResponse> getTransaction(@RequestParam("transactionId") Long transactionId) {
        TransactionResponse response = accountService.getTransaction(transactionId);
        return ResponseEntity.ok(response);
    }
}