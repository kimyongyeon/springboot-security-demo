package com.kyy.springbootsecuritydemo.common.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() { return "home (permitAll)"; }

    @GetMapping("/api/public/ping")
    public String pub() { return "public ok"; }

    @GetMapping("/api/user/ping")
    public String user() { return "user ok"; }

    @GetMapping("/api/admin/ping")
    public String admin() { return "admin ok"; }
}
