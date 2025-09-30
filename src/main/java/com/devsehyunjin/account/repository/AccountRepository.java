package com.devsehyunjin.account.repository;

import com.devsehyunjin.account.domain.Account;
import com.devsehyunjin.account.dto.CheckAccountResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // 특정 사용자 ID로 계좌 목록 검색
    List<Account> findByUserId(Long userId);

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByAccountNumber(String accountNumber);


}
