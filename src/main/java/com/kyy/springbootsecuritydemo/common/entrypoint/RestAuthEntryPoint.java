package com.kyy.springbootsecuritydemo.common.entrypoint;

import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class RestAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("""
            {"timestamp":"%s","status":401,"error":"Unauthorized","code":"AUTH_REQUIRED","message":"인증이 필요합니다.","path":"%s"}
            """.formatted(java.time.Instant.now().toString(), req.getRequestURI()));
    }
}