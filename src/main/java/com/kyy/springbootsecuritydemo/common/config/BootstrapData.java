package com.kyy.springbootsecuritydemo.common.config;

import com.kyy.springbootsecuritydemo.common.security.domain.entity.UserAccount;
import com.kyy.springbootsecuritydemo.common.security.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class BootstrapData {

    @Bean
    CommandLineRunner initUsers(UserAccountRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("user").isEmpty()) {
                repo.save(new UserAccount("user", encoder.encode("password"), Set.of("ROLE_USER")));
            }
            if (repo.findByUsername("admin").isEmpty()) {
                repo.save(new UserAccount("admin", encoder.encode("password"), Set.of("ROLE_ADMIN")));
            }
        };
    }
}
