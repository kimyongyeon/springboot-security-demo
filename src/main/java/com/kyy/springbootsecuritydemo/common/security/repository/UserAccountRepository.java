package com.kyy.springbootsecuritydemo.common.security.repository;

import com.kyy.springbootsecuritydemo.common.security.domain.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
}