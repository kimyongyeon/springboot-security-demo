package com.kyy.springbootsecuritydemo.common.interceptor;


import jakarta.servlet.http.*;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

public class TraceInterceptor implements HandlerInterceptor {
    public static final String ATTR_TRACE_ID = "traceId";
    public static final String ATTR_START_AT = "startAt";

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        String traceId = req.getHeader("X-Request-Id");
        if (traceId == null || traceId.isBlank()) traceId = UUID.randomUUID().toString();
        req.setAttribute(ATTR_TRACE_ID, traceId);
        req.setAttribute(ATTR_START_AT, System.currentTimeMillis());
        MDC.put(ATTR_TRACE_ID, traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        Object t = req.getAttribute(ATTR_TRACE_ID);
        if (t != null) res.setHeader("X-Request-Id", t.toString());
        MDC.remove(ATTR_TRACE_ID);
    }

    /** 유틸: 현재 요청 경과 시간 */
    public static Long durationMs(HttpServletRequest req) {
        Object start = req.getAttribute(ATTR_START_AT);
        if (start instanceof Long s) return System.currentTimeMillis() - s;
        return null;
    }
}