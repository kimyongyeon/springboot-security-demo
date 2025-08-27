package com.kyy.springbootsecuritydemo.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//@Component
@Order(Ordered.HIGHEST_PRECEDENCE )
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (response.isCommitted()){
            return;
        }

        try {
                jwtProvider.validateToken(); // 여기서 예외가 터질 수 있음
        } catch (ExpiredJwtException e) {
            // 예외를 직접 잡아서 여기서 바로 에러 응답을 생성
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다.");
            return; // 필터 체인 실행을 중단하고 즉시 리턴
        } catch (MalformedJwtException | SecurityException e) {
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "토큰이 유효하지 않습니다.");
            return; // 필터 체인 실행을 중단하고 즉시 리턴
        }  catch (Exception e) {
            setErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN_ERROR", "토큰 처리 중 오류가 발생했습니다.");
            return; // 필터 체인 실행을 중단하고 즉시 리턴
        }

        // 예외가 없을 때만 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    private class ErrorResponse {
        public String code;
        public String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    private void setErrorResponse(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        // ... 에러 응답 생성 로직 ...
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(code, message)));


    }
}
