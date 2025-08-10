package com.kyy.springbootsecuritydemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class SpringbootSecurityDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootSecurityDemoApplication.class, args);
    }

}
