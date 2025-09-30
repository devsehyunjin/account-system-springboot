package com.devsehyunjin.account.service;

import com.devsehyunjin.account.domain.Account;
import com.devsehyunjin.account.domain.Transaction;
import com.devsehyunjin.account.domain.User;
import com.devsehyunjin.account.domain.enums.AccountStatus;
import com.devsehyunjin.account.domain.enums.TransactionResult;
import com.devsehyunjin.account.domain.enums.TransactionType;
import com.devsehyunjin.account.dto.*;
import com.devsehyunjin.account.repository.AccountRepository;
import com.devsehyunjin.account.repository.TransactionRepository;
import com.devsehyunjin.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 계좌 생성 가능 여부 확인
        if (accountRepository.findByUserId(request.getUserId()).size() >= 10) {
            throw new RuntimeException("사용자가 생성 가능한 계좌 수를 초과했습니다.");
        }

        // 10자리 계좌번호 생
        String accountNumber = generateUniqueAccountNumber();

        // 새로운 계좌 생성
        Account newAccount = Account.createAccount(accountNumber, user, request.getInitialBalance());
        accountRepository.save(newAccount);

        return CreateAccountResponse.builder()
                .userId(newAccount.getUser().getId())
                .accountNumber(newAccount.getAccountNumber())
                .registeredAt(newAccount.getCreatedAt())
                .build();
    }

    // 계좌 해지
    @Transactional
    public CloseAccountResponse closeAccount(CloseAccountRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 2. 계좌 확인
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다."));

        // 3. 계좌 소유주와 사용자 검증
        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("사용자 아이디와 계좌 소유주가 다릅니다.");
        }

        // 4. 계좌 상태 검증 (이미 해지된 경우 실패)
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new RuntimeException("이미 해지된 계좌입니다.");
        }

        // 5. 잔액 확인 (잔액이 남아있는 경우 실패)
        if (account.getBalance() > 0) {
            throw new RuntimeException("잔액이 남아있어 계좌를 해지할 수 없습니다.");
        }

        // 6. 계좌 해지 (비즈니스 로직 실행)
        account.closeAccount();
        accountRepository.save(account); // 상태 변경 후 저장

        return CloseAccountResponse.builder()
                .userId(account.getUser().getId())
                .accountNumber(account.getAccountNumber())
                .closedAt(account.getClosedAt())
                .build();
    }

    // 계좌 확인
    @Transactional(readOnly = true)
    public List<CheckAccountResponse> getUserAccounts(Long userId) {
        // 사용자 존재 여부 확인
        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            throw new RuntimeException("User with ID " + userId + " does not exist.");
        }

        // 계좌 조회
        List<Account> accounts = accountRepository.findByUserId(userId);
        if (accounts.isEmpty()) {
            throw new RuntimeException("No accounts found for user ID " + userId);
        }

        // DTO로 변환하여 반환
        return accounts.stream()
                .map(account -> CheckAccountResponse
                        .builder()
                        .accountNumber(account.getAccountNumber())
                        .balance(account.getBalance())
                        .build())
                .collect(Collectors.toList());
    }

    // 잔액 사용
    public TransactionResponse useBalance(TransactionRequest request) {
        // Step 1: 사용자 확인
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        // Step 2: 계좌 확인
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("계좌가 존재하지 않습니다."));

        if (!account.getUser().equals(user)) {
            throw new RuntimeException("계좌 소유주가 아닙니다.");
        }

        // Step 3: 계좌 상태 확인
        if (account.isClosed()) {
            throw new RuntimeException("이미 해지된 계좌입니다.");
        }

        // Step 4: 거래 금액 유효성 확인
        if (request.getAmount() <= 0 || request.getAmount() > 1_000_000) {
            throw new RuntimeException("거래 금액이 유효하지 않습니다.");
        }

        if (account.getBalance() < request.getAmount()) {
            throw new RuntimeException("잔액이 부족합니다.");
        }

        // 계좌 잔액 사용
        account.updateBalanceForUse(request.getAmount());
        accountRepository.save(account);

        // Step 6: 트랜잭션 저장
        Transaction transaction = transactionRepository.save(
                Transaction.builder()
                        .account(account)
                        .transactionResult(TransactionResult.SUCCESS)
                        .transactionType(TransactionType.USE)
                        .amount(request.getAmount())
                        .transactionDate(LocalDateTime.now())
                        .build()
        );

        return TransactionResponse.from(transaction);
    }

    // 잔액 사용 취소
    public TransactionResponse cancelBalance(CancelTransactionRequest request) {
        // Step 1: 기존 거래 조회
        Transaction existingTransaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new RuntimeException("거래가 존재하지 않습니다."));

        // Step 2: 거래 및 계좌 검증
        Account account = existingTransaction.getAccount();

        if (!account.getAccountNumber().equals(request.getAccountNumber())) {
            throw new RuntimeException("해당 거래가 계좌와 일치하지 않습니다.");
        }

        if (!existingTransaction.getAmount().equals(request.getAmount())) {
            throw new RuntimeException("원거래 금액과 취소 금액이 일치하지 않습니다.");
        }

        // Step 3: 계좌 잔액 복원
        account.updateBalanceForCancel(request.getAmount());
        accountRepository.save(account);

        // Step 4: 취소 트랜잭션 저장
        Transaction cancelTransaction = transactionRepository.save(
                Transaction.builder()
                        .account(account)
                        .transactionType(TransactionType.CANCEL)
                        .transactionResult(TransactionResult.SUCCESS)
                        .amount(request.getAmount())
                        .transactionDate(LocalDateTime.now())
                        .build()
        );

        return TransactionResponse.from(cancelTransaction);
    }

    // 거래 확인
    public TransactionResponse getTransaction(Long transactionId) {
        // Step 1: Transaction ID로 거래 조회
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("거래가 존재하지 않습니다."));

        // Step 2: Transaction 정보를 DTO로 변환
        return TransactionResponse.fromCheck(transaction);
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        Random random = new Random();
        do {
            // 10자리 숫자 랜덤 생성
            accountNumber = String.format("%010d", Math.abs(random.nextLong() % 1_000_000_0000L));
        } while (accountRepository.existsByAccountNumber(accountNumber)); // 중복 확인

        return accountNumber;
    }
}