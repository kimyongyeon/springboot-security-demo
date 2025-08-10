package com.kyy.springbootsecuritydemo.common.entrypoint;

import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class RestAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        res.setStatus(HttpStatus.FORBIDDEN.value());
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("""
            {"timestamp":"%s","status":403,"error":"Forbidden","code":"ACCESS_DENIED","message":"접근 권한이 없습니다.","path":"%s"}
            """.formatted(java.time.Instant.now().toString(), req.getRequestURI()));
    }
}
