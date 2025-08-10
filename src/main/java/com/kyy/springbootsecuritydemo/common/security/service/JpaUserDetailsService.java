package com.kyy.springbootsecuritydemo.common.security.service;


import com.kyy.springbootsecuritydemo.common.security.domain.entity.UserAccount;
import com.kyy.springbootsecuritydemo.common.security.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;


@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserAccountRepository repo;

    public JpaUserDetailsService(UserAccountRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount ua = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user: " + username));
        return User.builder()
                .username(ua.getUsername())
                .password(ua.getPassword())
                .authorities(
                        ua.getRoles().stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toSet())
                )
                .disabled(!ua.isEnabled())
                .build();
    }
}
