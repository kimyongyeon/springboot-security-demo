package com.kyy.springbootsecuritydemo.work.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping("/api/public")
@RestController
public class HiController {

    @GetMapping("/hi")
    public void hi(HttpServletResponse res) throws IOException {
        var w = res.getWriter();      // ✔ Writer 먼저
        w.println("hello");
        res.getOutputStream();
//        return "hi";
    }
}
