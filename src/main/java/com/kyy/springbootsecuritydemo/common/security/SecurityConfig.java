package com.kyy.springbootsecuritydemo.common.security;

import com.kyy.springbootsecuritydemo.common.entrypoint.RestAccessDeniedHandler;
import com.kyy.springbootsecuritydemo.common.entrypoint.RestAuthEntryPoint;
import com.kyy.springbootsecuritydemo.common.security.filter.JwtAuthenticationFilter;
import com.kyy.springbootsecuritydemo.common.security.jwt.JwtTokenProvider;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final UserDetailsService uds;
    private final JwtTokenProvider jwt;

    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                String.valueOf(PathRequest.toStaticResources().atCommonLocations()), // /css/**, /js/**, /images/**, /webjars/**
                "/favicon.ico",
                "/error" // 스프링 기본 에러 페이지
        );
    }

    public SecurityConfig(UserDetailsService uds, JwtTokenProvider jwt) {
        this.uds = uds; this.jwt = jwt;
    }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean AuthenticationManager authenticationManager() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean RestAuthEntryPoint restAuthEntryPoint() { return new RestAuthEntryPoint(); }
    @Bean RestAccessDeniedHandler restAccessDeniedHandler() { return new RestAccessDeniedHandler(); }

//    /** 1) 공개 체인: 로그인/H2/문서/공개 API */
//    @Bean @Order(1)
//    SecurityFilterChain publicChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher(PublicEndpoints.PUBLIC) // 이 경로들에만 이 체인 적용
//                .csrf(csrf -> csrf.disable())
//                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // H2
//                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .httpBasic(Customizer.withDefaults())
//                .formLogin(f -> f.disable());
//        return http.build();
//    }
//
//    /** 2) 보호 체인: /api/** 등 나머지 */
//    @Bean @Order(2)
//    SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/api/user/**").hasAnyRole("USER","ADMIN")
//                        .anyRequest().authenticated()
//                )
//                .exceptionHandling(ex -> ex
//                        .authenticationEntryPoint(restAuthEntryPoint())
//                        .accessDeniedHandler(restAccessDeniedHandler())
//                )
//                .httpBasic(h -> h.disable())
//                .formLogin(f -> f.disable());
//
//        // JWT 필터: UsernamePasswordAuthenticationFilter 이전
//        http.addFilterBefore(
//                new JwtAuthenticationFilter(jwt, uds),
//                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
//        );
//
//        return http.build();
//    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.sameOrigin())) // H2 콘솔
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**", "/api/public/**", "/auth/login").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("USER","ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthEntryPoint())
                        .accessDeniedHandler(restAccessDeniedHandler())
                )
                .httpBasic(Customizer.withDefaults()) // 필요 없으면 .disable()
                .formLogin(f -> f.disable());

        // JWT 인증 필터 추가 (UsernamePasswordAuthenticationFilter 이전)
        http.addFilterBefore(new JwtAuthenticationFilter(jwt, uds),
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        var conf = new org.springframework.web.cors.CorsConfiguration();
        conf.setAllowedOrigins(java.util.List.of("http://localhost"));
        conf.setAllowedMethods(java.util.List.of("GET","POST","PUT","DELETE","OPTIONS"));
        conf.setAllowedHeaders(java.util.List.of("Authorization","Content-Type"));
        conf.setExposedHeaders(java.util.List.of("Authorization"));
        conf.setAllowCredentials(true);
        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", conf);
        return source;
    }
}