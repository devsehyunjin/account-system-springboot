package com.devsehyunjin.account.service;

import com.devsehyunjin.account.domain.Account;
import com.devsehyunjin.account.domain.Transaction;
import com.devsehyunjin.account.domain.User;
import com.devsehyunjin.account.domain.enums.TransactionResult;
import com.devsehyunjin.account.domain.enums.TransactionType;
import com.devsehyunjin.account.dto.*;
import com.devsehyunjin.account.repository.AccountRepository;
import com.devsehyunjin.account.repository.TransactionRepository;
import com.devsehyunjin.account.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService 테스트")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("테스트 사용자")
                .build();

        testAccount = Account.builder()
                .accountNumber("1234567890")
                .user(testUser)
                .initialBalance(10000L)
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .account(testAccount)
                .amount(1000L)
                .transactionType(TransactionType.USE)
                .transactionResult(TransactionResult.SUCCESS)
                .transactionDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("계좌 생성 성공")
    void createAccount_Success() {
        // given
        CreateAccountRequest request = new CreateAccountRequest(1L, 10000L);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByUserId(1L)).willReturn(Arrays.asList());
        given(accountRepository.existsByAccountNumber(anyString())).willReturn(false);
        given(accountRepository.save(any(Account.class))).willReturn(testAccount);

        // when
        CreateAccountResponse response = accountService.createAccount(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getAccountNumber()).isNotEmpty(); // 계좌번호는 랜덤 생성되므로 비어있지 않기만 확인
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("계좌 생성 실패 - 사용자 없음")
    void createAccount_UserNotFound() {
        // given
        CreateAccountRequest request = new CreateAccountRequest(999L, 10000L);

        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("계좌 생성 실패 - 계좌 수 초과")
    void createAccount_ExceedMaxAccounts() {
        // given
        CreateAccountRequest request = new CreateAccountRequest(1L, 10000L);

        // 10개 계좌가 이미 존재하는 상황
        List<Account> existingAccounts = Arrays.asList(
                createMockAccount(), createMockAccount(), createMockAccount(), createMockAccount(), createMockAccount(),
                createMockAccount(), createMockAccount(), createMockAccount(), createMockAccount(), createMockAccount()
        );

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByUserId(1L)).willReturn(existingAccounts);

        // when & then
        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("사용자가 생성 가능한 계좌 수를 초과했습니다.");
    }

    @Test
    @DisplayName("계좌 해지 성공")
    void closeAccount_Success() {
        // given
        CloseAccountRequest request = new CloseAccountRequest(1L, "1234567890");

        // 잔액이 0인 계좌 생성
        Account accountWithZeroBalance = Account.builder()
                .accountNumber("1234567890")
                .user(testUser)
                .initialBalance(0L)
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber("1234567890")).willReturn(Optional.of(accountWithZeroBalance));
        given(accountRepository.save(any(Account.class))).willReturn(accountWithZeroBalance);

        // when
        CloseAccountResponse response = accountService.closeAccount(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getAccountNumber()).isEqualTo("1234567890");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("계좌 해지 실패 - 사용자 없음")
    void closeAccount_UserNotFound() {
        // given
        CloseAccountRequest request = new CloseAccountRequest(999L, "1234567890");

        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.closeAccount(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("계좌 해지 실패 - 계좌 없음")
    void closeAccount_AccountNotFound() {
        // given
        CloseAccountRequest request = new CloseAccountRequest(1L, "9999999999");

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber("9999999999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.closeAccount(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("계좌를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("계좌 해지 실패 - 소유주 불일치")
    void closeAccount_OwnerMismatch() {
        // given
        CloseAccountRequest request = new CloseAccountRequest(2L, "1234567890"); // 다른 사용자 ID

        // 다른 사용자 생성
        User otherUser = User.builder()
                .id(2L)
                .name("다른 사용자")
                .build();

        // 잔액이 0인 계좌 생성 (잔액 검증을 통과하기 위해)
        Account accountWithZeroBalance = Account.builder()
                .accountNumber("1234567890")
                .user(testUser) // testUser(1L)가 소유한 계좌
                .initialBalance(0L)
                .build();

        given(userRepository.findById(2L)).willReturn(Optional.of(otherUser));
        given(accountRepository.findByAccountNumber("1234567890")).willReturn(Optional.of(accountWithZeroBalance));

        // when & then
        assertThatThrownBy(() -> accountService.closeAccount(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("사용자 아이디와 계좌 소유주가 다릅니다.");
    }

    @Test
    @DisplayName("계좌 해지 실패 - 이미 해지된 계좌")
    void closeAccount_AlreadyClosed() {
        // given
        CloseAccountRequest request = new CloseAccountRequest(1L, "1234567890");

        // 이미 해지된 계좌 생성
        Account closedAccount = Account.builder()
                .accountNumber("1234567890")
                .user(testUser)
                .initialBalance(0L)
                .build();
        closedAccount.closeAccount(); // 계좌 해지

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber("1234567890")).willReturn(Optional.of(closedAccount));

        // when & then
        assertThatThrownBy(() -> accountService.closeAccount(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이미 해지된 계좌입니다.");
    }

    @Test
    @DisplayName("계좌 해지 실패 - 잔액 남음")
    void closeAccount_BalanceRemaining() {
        // given
        CloseAccountRequest request = new CloseAccountRequest(1L, "1234567890");

        // 잔액이 남아있는 계좌 생성
        Account accountWithBalance = Account.builder()
                .accountNumber("1234567890")
                .user(testUser)
                .initialBalance(1000L) // 잔액이 남아있음
                .build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber("1234567890")).willReturn(Optional.of(accountWithBalance));

        // when & then
        assertThatThrownBy(() -> accountService.closeAccount(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("잔액이 남아있어 계좌를 해지할 수 없습니다.");
    }

    @Test
    @DisplayName("사용자 계좌 조회 성공")
    void getUserAccounts_Success() {
        // given
        Long userId = 1L;
        List<Account> accounts = Arrays.asList(testAccount);

        given(userRepository.existsById(userId)).willReturn(true);
        given(accountRepository.findByUserId(userId)).willReturn(accounts);

        // when
        List<CheckAccountResponse> response = accountService.getUserAccounts(userId);

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getAccountNumber()).isEqualTo("1234567890");
        assertThat(response.get(0).getBalance()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("사용자 계좌 조회 실패 - 사용자 없음")
    void getUserAccounts_UserNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.existsById(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> accountService.getUserAccounts(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User with ID 999 does not exist.");
    }

    @Test
    @DisplayName("사용자 계좌 조회 실패 - 계좌 없음")
    void getUserAccounts_NoAccounts() {
        // given
        Long userId = 1L;
        given(userRepository.existsById(userId)).willReturn(true);
        given(accountRepository.findByUserId(userId)).willReturn(Arrays.asList());

        // when & then
        assertThatThrownBy(() -> accountService.getUserAccounts(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No accounts found for user ID 1");
    }

    @Test
    @DisplayName("잔액 사용 성공")
    void useBalance_Success() {
        // given
        TransactionRequest request = new TransactionRequest(1L, "1234567890", 1000L);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber("1234567890")).willReturn(Optional.of(testAccount));
        given(accountRepository.save(any(Account.class))).willReturn(testAccount);
        given(transactionRepository.save(any(Transaction.class))).willReturn(testTransaction);

        // when
        TransactionResponse response = accountService.useBalance(request);

        // then
        assertThat(response).isNotNull();
        verify(accountRepository).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("잔액 사용 실패 - 사용자 없음")
    void useBalance_UserNotFound() {
        // given
        TransactionRequest request = new TransactionRequest(999L, "1234567890", 1000L);

        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.useBalance(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("사용자가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("잔액 사용 실패 - 계좌 없음")
    void useBalance_AccountNotFound() {
        // given
        TransactionRequest request = new TransactionRequest(1L, "9999999999", 1000L);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber("9999999999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.useBalance(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("계좌가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("잔액 사용 실패 - 소유주 불일치")
    void useBalance_OwnerMismatch() {
        // given
        TransactionRequest request = new TransactionRequest(2L, "1234567890", 1000L);

        User otherUser = User.builder().id(2L).name("다른 사용자").build();

        given(userRepository.findById(2L)).willReturn(Optional.of(otherUser));
        given(accountRepository.findByAccountNumber("1234567890")).willReturn(Optional.of(testAccount));

        // when & then
        assertThatThrownBy(() -> accountService.useBalance(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("계좌 소유주가 아닙니다.");
    }

    @Test
    @DisplayName("잔액 사용 실패 - 해지된 계좌")
    void useBalance_ClosedAccount() {
        // given
        TransactionRequest request = new TransactionRequest(1L, "1234567890", 1000L);

        // 해지된 계좌 생성
        Account closedAccount = Account.builder()
                .accountNumber("1234567890")
                .user(testUser)
                .initialBalance(10000L)
                .build();
        closedAccount.closeAccount(); // 계좌 해지

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber("1234567890")).willReturn(Optional.of(closedAccount));

        // when & then
        assertThatThrownBy(() -> accountService.useBalance(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이미 해지된 계좌입니다.");
    }

    @Test
    @DisplayName("잔액 사용 실패 - 유효하지 않은 금액")
    void useBalance_InvalidAmount() {
        // given
        TransactionRequest request = new TransactionRequest(1L, "1234567890", 0L); // 유효하지 않은 금액

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber("1234567890")).willReturn(Optional.of(testAccount));

        // when & then
        assertThatThrownBy(() -> accountService.useBalance(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("거래 금액이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("잔액 사용 실패 - 금액 초과")
    void useBalance_AmountExceeded() {
        // given
        TransactionRequest request = new TransactionRequest(1L, "1234567890", 2000000L); // 1,000,000 초과

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber("1234567890")).willReturn(Optional.of(testAccount));

        // when & then
        assertThatThrownBy(() -> accountService.useBalance(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("거래 금액이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("잔액 사용 실패 - 잔액 부족")
    void useBalance_InsufficientBalance() {
        // given
        TransactionRequest request = new TransactionRequest(1L, "1234567890", 20000L); // 잔액보다 큰 금액

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(accountRepository.findByAccountNumber("1234567890")).willReturn(Optional.of(testAccount));

        // when & then
        assertThatThrownBy(() -> accountService.useBalance(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("잔액이 부족합니다.");
    }

    @Test
    @DisplayName("잔액 사용 취소 성공")
    void cancelBalance_Success() {
        // given
        CancelTransactionRequest request = new CancelTransactionRequest(1L, "1234567890", 1000L);

        given(transactionRepository.findById(1L)).willReturn(Optional.of(testTransaction));
        given(accountRepository.save(any(Account.class))).willReturn(testAccount);
        given(transactionRepository.save(any(Transaction.class))).willReturn(testTransaction);

        // when
        TransactionResponse response = accountService.cancelBalance(request);

        // then
        assertThat(response).isNotNull();
        verify(accountRepository).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("잔액 사용 취소 실패 - 거래 없음")
    void cancelBalance_TransactionNotFound() {
        // given
        CancelTransactionRequest request = new CancelTransactionRequest(999L, "1234567890", 1000L);

        given(transactionRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.cancelBalance(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("거래가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("잔액 사용 취소 실패 - 계좌 불일치")
    void cancelBalance_AccountMismatch() {
        // given
        CancelTransactionRequest request = new CancelTransactionRequest(1L, "9999999999", 1000L); // 다른 계좌번호

        given(transactionRepository.findById(1L)).willReturn(Optional.of(testTransaction));

        // when & then
        assertThatThrownBy(() -> accountService.cancelBalance(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("해당 거래가 계좌와 일치하지 않습니다.");
    }

    @Test
    @DisplayName("잔액 사용 취소 실패 - 금액 불일치")
    void cancelBalance_AmountMismatch() {
        // given
        CancelTransactionRequest request = new CancelTransactionRequest(1L, "1234567890", 2000L); // 다른 금액

        given(transactionRepository.findById(1L)).willReturn(Optional.of(testTransaction));

        // when & then
        assertThatThrownBy(() -> accountService.cancelBalance(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("원거래 금액과 취소 금액이 일치하지 않습니다.");
    }

    @Test
    @DisplayName("거래 조회 성공")
    void getTransaction_Success() {
        // given
        Long transactionId = 1L;
        given(transactionRepository.findById(transactionId)).willReturn(Optional.of(testTransaction));

        // when
        TransactionResponse response = accountService.getTransaction(transactionId);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("거래 조회 실패 - 거래 없음")
    void getTransaction_NotFound() {
        // given
        Long transactionId = 999L;
        given(transactionRepository.findById(transactionId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.getTransaction(transactionId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("거래가 존재하지 않습니다.");
    }

    // 헬퍼 메서드
    private Account createMockAccount() {
        return Account.builder()
                .accountNumber("1234567890")
                .user(testUser)
                .initialBalance(1000L)
                .build();
    }
}