package com.kyy.springbootsecuritydemo.common.config;

import com.kyy.springbootsecuritydemo.common.interceptor.TraceInterceptor;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry reg) {
        reg.addInterceptor(new TraceInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns( // 정적/문서/H2 등 제외
                        "/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**",
                        "/css/**", "/js/**", "/images/**", "/webjars/**"
                );
    }

}