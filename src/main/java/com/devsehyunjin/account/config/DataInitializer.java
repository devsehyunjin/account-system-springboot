package com.devsehyunjin.account.config;

import com.devsehyunjin.account.domain.User;
import com.devsehyunjin.account.repository.AccountRepository;
import com.devsehyunjin.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;

    @Bean
    public ApplicationRunner initializeData() {
        return args -> {
            // 사용자 초기화
            User user1 = User.builder()
                    .name("Alice")
                    .createdAt(LocalDateTime.now())
                    .build();

            User user2 = User.builder()
                    .name("Bob")
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(user1);
            userRepository.save(user2);

            System.out.println("✅ 초기 데이터가 성공적으로 저장되었습니다.");
        };
    }
}
