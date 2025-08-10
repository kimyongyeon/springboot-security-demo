package com.kyy.springbootsecuritydemo.common.logs;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<TraceFilter> traceFilter() {
        var bean = new FilterRegistrationBean<>(new TraceFilter());
        bean.setOrder(Integer.MIN_VALUE); // 최전방
        return bean;
    }
}
