package com.kyy.springbootsecuritydemo.common.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
/**
 * 요청 헤더의 역할 목록을 읽어 현재 Authentication의 권한을 보강/검증하는 필터
 * - 헤더명: X-Auth-Roles (예: "USER,ADMIN")
 * - allowList: 허용 가능한 권한 화이트리스트(미지정 시 모두 허용)
 * - requireOnPaths: 특정 경로 패턴에 대해 필수 권한을 요구(없으면 403)
 */
public class AuthorityFilter extends OncePerRequestFilter {

    private final String headerName;
    private final boolean autoPrefixRole;   // "ROLE_" 자동 프리픽스 여부
    private final Set<String> allowList;    // 허용 권한(ROLE_ 포함 형태)
    private final Map<String, Set<String>> requireOnPaths; // pathPattern -> required roles
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthorityFilter(
            String headerName,
            boolean autoPrefixRole,
            Set<String> allowList,
            Map<String, Set<String>> requireOnPaths
    ) {
        this.headerName = headerName != null ? headerName : "X-Auth-Roles";
        this.autoPrefixRole = autoPrefixRole;
        this.allowList = allowList != null ? allowList : Collections.emptySet();
        this.requireOnPaths = requireOnPaths != null ? requireOnPaths : Collections.emptyMap();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 예: OPTIONS 프리플라이트는 스킵
        return HttpMethod.OPTIONS.matches(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

        // 1) 헤더에서 신규 역할 파싱
        Set<String> headerRoles = parseRoles(req.getHeader(headerName));

        // 2) 허용 리스트 제한이 있으면 필터링
        if (!allowList.isEmpty()) {
            headerRoles = headerRoles.stream()
                    .filter(allowList::contains)
                    .collect(Collectors.toSet());
        }

        // 3) 인증이 있다면 권한 보강
        if (currentAuth != null && currentAuth.isAuthenticated() && !headerRoles.isEmpty()) {
            Collection<? extends GrantedAuthority> merged =
                    mergeAuthorities(currentAuth.getAuthorities(), toAuthorities(headerRoles));

            Authentication upgraded = new UsernamePasswordAuthenticationToken(
                    currentAuth.getPrincipal(),
                    currentAuth.getCredentials(),
                    merged
            );
            // details 유지
            if (currentAuth instanceof AbstractAuthenticationToken token) {
                ((AbstractAuthenticationToken) upgraded).setDetails(token.getDetails());
            }
            SecurityContextHolder.getContext().setAuthentication(upgraded);
            currentAuth = upgraded;
        }

        // 4) 경로별 필수 권한 검사(인증이 있든 없든 적용)
        if (!requireOnPaths.isEmpty()) {
            for (Map.Entry<String, Set<String>> e : requireOnPaths.entrySet()) {
                if (pathMatcher.match(e.getKey(), req.getRequestURI())) {
                    Set<String> required = e.getValue();
                    if (!hasAnyAuthority(currentAuth, required)) {
                        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        res.setContentType("application/json");
                        res.getWriter().write("{\"message\":\"forbidden: missing required authority\"}");
                        return;
                    }
                }
            }
        }

        chain.doFilter(req, res);
    }

    private Set<String> parseRoles(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) return Collections.emptySet();
        return Arrays.stream(headerValue.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(this::normalizeRole)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizeRole(String r) {
        if (!autoPrefixRole) return r;
        return r.startsWith("ROLE_") ? r : ("ROLE_" + r);
    }

    private boolean hasAnyAuthority(Authentication auth, Set<String> required) {
        if (required == null || required.isEmpty()) return true; // 요구 없음
        if (auth == null || !auth.isAuthenticated()) return false;

        Set<String> have = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        for (String need : required) {
            if (have.contains(need)) return true;
        }
        return false;
    }

    private Collection<? extends GrantedAuthority> toAuthorities(Set<String> roles) {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    private Collection<? extends GrantedAuthority> mergeAuthorities(
            Collection<? extends GrantedAuthority> a,
            Collection<? extends GrantedAuthority> b
    ) {
        Set<String> names = new LinkedHashSet<>();
        a.forEach(g -> names.add(g.getAuthority()));
        b.forEach(g -> names.add(g.getAuthority()));
        return names.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}