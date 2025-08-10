package com.kyy.springbootsecuritydemo.common.security.controller;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo")
public class ErrorDemoController {

    @GetMapping("/entity")
    public String notFound() {
        throw new EntityNotFoundException("샘플 엔티티를 찾을 수 없습니다.");
    }

    @PostMapping("/integrity")
    public String conflict() {
        throw new DataIntegrityViolationException("고유키 위반 샘플");
    }

    @GetMapping("/server-error")
    public String boom() {
        throw new RuntimeException("테스트용 500");
    }

    @GetMapping("/ok")
    public String ok() { return "ok"; }
}