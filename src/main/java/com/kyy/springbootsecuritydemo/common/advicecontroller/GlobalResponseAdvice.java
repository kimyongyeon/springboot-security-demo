package com.kyy.springbootsecuritydemo.common.advicecontroller;

import com.kyy.springbootsecuritydemo.common.interceptor.TraceInterceptor;
import com.kyy.springbootsecuritydemo.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    private final HttpServletRequest request;

    public GlobalResponseAdvice(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 필요 시 특정 패키지/어노테이션만 적용하도록 제한 가능
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest serverHttpRequest,
            ServerHttpResponse serverHttpResponse) {

        // 1) 스킵 조건: 이미 래핑됨, 스트리밍/리소스/바이너리, HTML
        if (body instanceof ApiResponse<?>) return body;
        if (body instanceof org.springframework.core.io.Resource) return body;
        if (org.springframework.http.MediaType.TEXT_HTML.includes(selectedContentType)) return body;
        if (body instanceof org.springframework.http.ProblemDetail) return body; // Spring 기본 오류 포맷 사용 시

        // 2) 공통 정보 채우기
        String path = request.getServletPath();
        String traceId = (String) request.getAttribute(TraceInterceptor.ATTR_TRACE_ID);
        Long duration = TraceInterceptor.durationMs(request);

        // 3) ResponseEntity면 내부 바디를 감싸서 재구성
        if (body instanceof ResponseEntity<?> re) {
            Object realBody = re.getBody();
            ApiResponse<Object> wrapped = ApiResponse.ok(realBody, path, traceId, duration);
            return ResponseEntity.status(re.getStatusCode())
                    .headers(re.getHeaders())
                    .body(wrapped);
        }

        // 4) 일반 바디
        return ApiResponse.ok(body, path, traceId, duration);
    }
}