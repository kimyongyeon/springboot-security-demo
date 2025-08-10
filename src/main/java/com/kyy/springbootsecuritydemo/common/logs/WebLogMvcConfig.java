package com.kyy.springbootsecuritydemo.common.logs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebLogMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry reg) {
        reg.addInterceptor(new IndentInterceptor()).addPathPatterns("/**");
    }
}
