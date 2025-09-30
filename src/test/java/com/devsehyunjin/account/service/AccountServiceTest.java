package com.devsehyunjin.account.service;

import com.devsehyunjin.account.domain.Account;
import com.devsehyunjin.account.domain.User;
import com.devsehyunjin.account.dto.CreateAccountRequest;
import com.devsehyunjin.account.dto.CreateAccountResponse;
import com.devsehyunjin.account.repository.AccountRepository;
import com.devsehyunjin.account.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    public AccountServiceTest() {
        MockitoAnnotations.openMocks(this); // Mock 초기화
    }

    @Test
    void createAccount_Success() {
        // given
        User user = User.builder()
                .id(1L)
                        .name()
                build();

        user.setId(1L); // 간단한 사용자 데이터 설정

        CreateAccountRequest request = new CreateAccountRequest(1L, 10000);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user)); // 사용자 Mock
        when(accountRepository.countByUser(user)).thenReturn(2);        // 계좌 수 Mock
        when(accountRepository.save(any(Account.class))).thenReturn(new Account());

        // when
        CreateAccountResponse response = accountService.createAccount(request);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getUserId()); // 사용자 ID 확인
        assertEquals(10000, response.getInitialBalance()); // 초기 잔액 확인
    }

    @Test
    void createAccount_Fail_UserNotFound() {
        // given
        CreateAccountRequest request = new CreateAccountRequest(99L, 10000);
        when(userRepository.findById(99L)).thenReturn(Optional.empty()); // 사용자 없음

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accountService.createAccount(request));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void createAccount_Fail_MaximumAccounts() {
        // given
        User user = new User();
        user.setId(1L); // 간단한 사용자 데이터 설정

        CreateAccountRequest request = new CreateAccountRequest(1L, 10000);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user)); // 사용자 Mock
        when(accountRepository.countByUser(user)).thenReturn(10);        // 최대 계좌 수 Mock

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accountService.createAccount(request));
        assertEquals("Maximum number of accounts reached", exception.getMessage());
    }
}