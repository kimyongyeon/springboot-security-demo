package com.kyy.springbootsecuritydemo.common.security.domain.vo;

import lombok.*;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserVo {
    private String username;
    private String password;
    private Set<String> roles;
    private boolean enabled;
}
