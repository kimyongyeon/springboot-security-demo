package com.kyy.springbootsecuritydemo.common.error;

import com.kyy.springbootsecuritydemo.common.interceptor.TraceInterceptor;
import com.kyy.springbootsecuritydemo.common.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 400 - 잘못된 요청 바디/파라미터
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), req);
    }

    // 400 - @Valid 바인딩 에러
    @ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })
    public ResponseEntity<ApiError> handleValidation(Exception ex, HttpServletRequest req) {
        var api = base(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력값을 확인하세요.", req);

        if (ex instanceof MethodArgumentNotValidException manv) {
            api.fieldErrors = manv.getBindingResult().getFieldErrors().stream()
                    .map(fe -> new ApiError.FieldError(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
                    .collect(Collectors.toList());
        } else if (ex instanceof BindException be) {
            api.fieldErrors = be.getBindingResult().getFieldErrors().stream()
                    .map(fe -> new ApiError.FieldError(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue()))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.status(api.status).body(api);
    }

    // 401 - 인증 실패(폼로그인/JWT 등)
    @ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
    public ResponseEntity<ApiError> handleAuthFail(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "인증에 실패했습니다.", req);
    }

    // 403 - 권한 부족 (필터/설정에서 못 잡혔을 때의 안전망)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다.", req);
    }

    // 404 - 라우팅 없음 (옵션: yml 설정 필요)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NoHandlerFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청하신 리소스를 찾을 수 없습니다.", req);
    }

    // 404/409 - 도메인 예외 예시
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY", "데이터 무결성 위반입니다.", req);
    }

    private String originalUri(HttpServletRequest req) {
        Object v = req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        return v != null ? v.toString() : req.getRequestURI();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAny(Exception ex, HttpServletRequest req) {
        String orig = originalUri(req);
        Integer sc = (Integer) req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String msg = (String) req.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        log.error("Unhandled error at {} (status={}, msg={})", orig, sc, msg, ex);
        return wrap(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "알 수 없는 오류가 발생했습니다.", req);
    }


//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest req) {
//        String orig = originalUri(req);
//        Integer sc = (Integer) req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
//        String msg = (String) req.getAttribute(RequestDispatcher.ERROR_MESSAGE);
//        log.error("Unhandled error at {} (status={}, msg={})", orig, sc, msg, ex);
//        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "알 수 없는 오류가 발생했습니다.", req);
//    }

    // 405 - 메서드 미지원
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethod(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", ex.getMessage(), req);
    }

    // 500 - 그 외 모든 예외
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest req) {
//        // 로깅은 여기서 남기면 좋다 (예: logger.error("Unhandled", ex))
//        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "알 수 없는 오류가 발생했습니다.", req);
//    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest req) {
//        log.error("Unhandled error at {}: {}", req.getRequestURI(), ex.getMessage(), ex); // ← 스택추적 남기기
//        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "알 수 없는 오류가 발생했습니다.", req);
//    }

    // -------------------------------

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String msg, HttpServletRequest req) {
        var api = base(status, code, msg, req);
        return ResponseEntity.status(status).body(api);
    }

    private ApiError base(HttpStatus status, String code, String msg, HttpServletRequest req) {
        var api = new ApiError();
        api.status  = status.value();
        api.error   = status.getReasonPhrase();
        api.code    = code;
        api.message = msg;
        api.path    = req.getRequestURI();
        // api.traceId = MDC.get("traceId"); // 관제 연동 시
        return api;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBad(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("BAD: {}", ex.getMessage());
        return wrap(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), req);
    }

    private ResponseEntity<ApiResponse<Void>> wrap(HttpStatus status, String code, String msg, HttpServletRequest req) {
        String path = req.getServletPath();
        String traceId = (String) req.getAttribute(TraceInterceptor.ATTR_TRACE_ID);
        Long duration = TraceInterceptor.durationMs(req);
        var body = ApiResponse.<Void>error(code, msg, path, traceId, duration);
        return ResponseEntity.status(status).body(body);
    }
}