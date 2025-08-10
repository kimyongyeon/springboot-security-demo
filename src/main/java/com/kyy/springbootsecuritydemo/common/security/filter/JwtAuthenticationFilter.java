package com.kyy.springbootsecuritydemo.common.security.filter;

import com.kyy.springbootsecuritydemo.common.entrypoint.PublicEndpoints;
import com.kyy.springbootsecuritydemo.common.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwt;
    private final UserDetailsService uds;

//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest req) {
//        String uri = req.getRequestURI();
//        return matches(uri, PublicEndpoints.PUBLIC)
//                || "OPTIONS".equalsIgnoreCase(req.getMethod()); // CORS 프리플라이트
//    }
//
//    private boolean matches(String uri, String[] patterns) {
//        var m = new org.springframework.util.AntPathMatcher();
//        for (String p : patterns) {
//            if (m.match(p, uri)) return true;
//        }
//        return false;
//    }

    public JwtAuthenticationFilter(JwtTokenProvider jwt, UserDetailsService uds) {
        this.jwt = jwt;
        this.uds = uds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String header = req.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res); // 토큰 없으면 그냥 통과

            // 반드시 체크 해야 하는가?
            // 체크 하는 이유가 이미 호출된건가?
//            if (!res.isCommitted()) {
//                if ("/api/public/hi".equals(req.getServletPath())) {
//                    // ⛔ 이미 getOutputStream()을 썼기 때문에 여기서 getWriter() 호출 시 예외
//                    res.getWriter().println("tail-append"); // -> IllegalStateException
//                }
//            }

            return;
        }
        try {
            var jws = jwt.parse(header.substring(7));
            var username = jws.getBody().getSubject();
            var user = uds.loadUserByUsername(username);
            var roles = (List<String>) jws.getBody().get("roles");
            var auth = new UsernamePasswordAuthenticationToken(
                    user, null,
                    roles == null ? user.getAuthorities() :
                            roles.stream().map(SimpleGrantedAuthority::new).toList());
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            // 여기서 예외 '던지지' 마세요. 보호 구간일 경우 EntryPoint가 401을 반환합니다.
        }
        chain.doFilter(req, res);
    }

//    @Override
//    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
//            throws ServletException, IOException {
//
//        String header = req.getHeader("Authorization");
//        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
//            String token = header.substring(7);
//            try {
//                Jws<Claims> jws = jwt.parse(token);
//                String username = jws.getBody().getSubject();
//                @SuppressWarnings("unchecked")
//                List<String> roles = (List<String>) jws.getBody().get("roles");
//
//                // DB에서 사용자 상태를 확인하고 싶다면 uds 사용
//                var user = uds.loadUserByUsername(username);
//
//                var authorities = roles == null ? user.getAuthorities()
//                        : roles.stream().map(SimpleGrantedAuthority::new).toList();
//
//                var auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
//                SecurityContextHolder.getContext().setAuthentication(auth);
//            } catch (Exception e) {
//                // 토큰 문제(만료/서명오류 등) → 컨텍스트 비움 (EntryPoint가 401 반환)
//                SecurityContextHolder.clearContext();
//            }
//        }
//
//        chain.doFilter(req, res);
//    }
}
