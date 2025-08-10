package com.kyy.springbootsecuritydemo.common.logs;

import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.DispatcherServlet;

@Configuration
public class ServletBeanConfig {
    @Bean
    public DispatcherServlet dispatcherServlet() {
        return new LoggingDispatcherServlet();
    }
    @Bean
    public DispatcherServletRegistrationBean dispatcherServletRegistration(DispatcherServlet ds) {
        return new DispatcherServletRegistrationBean(ds, "/");
    }
}
