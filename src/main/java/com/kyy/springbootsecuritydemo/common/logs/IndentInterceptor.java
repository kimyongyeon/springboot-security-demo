package com.kyy.springbootsecuritydemo.common.logs;

import jakarta.servlet.http.*;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public class IndentInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(IndentInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        IndentMdc.push("interceptor");
        log.info("preHandle {}", handler);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        log.info("afterCompletion {}", handler);
        IndentMdc.pop();
    }
}
