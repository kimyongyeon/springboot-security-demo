package com.kyy.springbootsecuritydemo.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    public Instant timestamp = Instant.now();
    public boolean success;
    public String code;     // 비즈니스 코드
    public String message;  // 사용자/로그용 메시지
    public String path;     // 요청 경로
    public String traceId;  // 요청 상관관계 ID
    public Long   durationMs; // 처리 시간
    public T data;          // 실데이터

    public static <T> ApiResponse<T> ok(T data, String path, String traceId, Long durationMs) {
        var r = new ApiResponse<T>();
        r.success = true; r.code = "OK"; r.message = "success";
        r.path = path; r.traceId = traceId; r.durationMs = durationMs; r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> error(String code, String message, String path, String traceId, Long durationMs) {
        var r = new ApiResponse<T>();
        r.success = false; r.code = code; r.message = message;
        r.path = path; r.traceId = traceId; r.durationMs = durationMs;
        return r;
    }
}