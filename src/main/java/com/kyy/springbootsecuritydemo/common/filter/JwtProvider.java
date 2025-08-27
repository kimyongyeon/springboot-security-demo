package com.kyy.springbootsecuritydemo.common.filter;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    public void validateToken() {
        throw new RuntimeException( "Token expired");
    }

}

