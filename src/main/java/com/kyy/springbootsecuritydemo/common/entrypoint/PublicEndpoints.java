package com.kyy.springbootsecuritydemo.common.entrypoint;

public final class PublicEndpoints {
    private PublicEndpoints() {}
    public static final String[] PUBLIC = {
            "/auth/login",
            "/api/public/**",
            "/actuator/health",
            "/h2-console/**",
            "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"
    };
}