package com.kyy.springbootsecuritydemo.common.logs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyy.springbootsecuritydemo.common.filter.JwtAuthenticationFilter;
import com.kyy.springbootsecuritydemo.common.filter.JwtProvider;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;

@Configuration
public class FilterConfig {

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    public FilterConfig(JwtProvider jwtProvider, ObjectMapper objectMapper) {
        this.jwtProvider = jwtProvider;
        this.objectMapper = objectMapper;
    }

    @Bean
    public FilterRegistrationBean<TraceFilter> traceFilter() {
        var bean = new FilterRegistrationBean<>(new TraceFilter());
        bean.setOrder(Integer.MIN_VALUE); // 최전방
        return bean;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();

        // 1. 등록할 필터 인스턴스를 설정합니다.
        registrationBean.setFilter(new JwtAuthenticationFilter(jwtProvider, objectMapper));

        // 2. 필터가 적용될 URL 패턴을 지정합니다.
        registrationBean.addUrlPatterns("/api/*");

        // 3. **가장 중요한 설정**: 필터가 적용될 Dispatcher 유형을 지정합니다.
        // 이 설정을 통해 최초의 HTTP 요청에만 필터가 적용되고,
        // 에러 발생으로 인한 내부적인 ERROR dispatch에는 필터가 적용되지 않습니다.
        registrationBean.setDispatcherTypes(DispatcherType.REQUEST);

        return registrationBean;
    }
}
