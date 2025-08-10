package com.kyy.springbootsecuritydemo.common.logs;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class TraceFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String traceId = req.getHeader("X-Request-Id");
        if (traceId == null || traceId.isBlank()) traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        res.setHeader("X-Request-Id", traceId);

        IndentMdc.push("filter");
        try {
            log.info("enter filter {}", req.getServletPath());
            chain.doFilter(req, res);
            log.info("leave filter {}", req.getServletPath());
        } finally {
            IndentMdc.pop();
            IndentMdc.clear();
            MDC.remove("traceId");
        }
    }
}
