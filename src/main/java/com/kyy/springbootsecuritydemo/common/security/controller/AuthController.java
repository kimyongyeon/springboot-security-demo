package com.kyy.springbootsecuritydemo.common.security.controller;

import com.kyy.springbootsecuritydemo.common.security.jwt.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwt;

    public AuthController(AuthenticationManager authManager, JwtTokenProvider jwt) {
        this.authManager = authManager;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        UserDetails principal = (UserDetails) auth.getPrincipal();
        String token = jwt.generate(principal);

        return ResponseEntity.ok(Map.of(
                "tokenType", "Bearer",
                "accessToken", token,
                "username", principal.getUsername(),
                "roles", principal.getAuthorities().stream().map(a -> a.getAuthority()).toList()
        ));
    }

    public record LoginRequest(String username, String password) {}
}