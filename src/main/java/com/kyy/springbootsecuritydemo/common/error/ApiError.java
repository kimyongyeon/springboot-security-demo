package com.kyy.springbootsecuritydemo.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    public Instant timestamp = Instant.now();
    public int status;
    public String error;      // HTTP reason phrase
    public String code;       // 비즈니스/내부 코드(Optional)
    public String message;    // 사용자 전달용 메시지
    public String path;       // 요청 URI
    public String traceId;    // MDC나 관제용 ID(Optional)
    public List<FieldError> fieldErrors; // 검증 오류 목록(Optional)

    public static class FieldError {
        public String field;
        public String reason;
        public Object rejectedValue;

        public FieldError(String field, String reason, Object rejectedValue) {
            this.field = field; this.reason = reason; this.rejectedValue = rejectedValue;
        }
    }
}