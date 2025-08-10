# Spring Security + JPA + H2 + JWT 보일러플레이트

**목표:** 바로 실행 가능한 최소 구성으로 시작해, `JPA + H2` 기반 사용자 관리, `JWT` 기반 인증(Stateless), 글로벌 예외 처리(JSON), 선택적 `AuthorityFilter`(헤더→권한 보강)까지 한 번에 갖춘 스타터 템플릿.

---

## 핵심 요약

* **스택**: Spring Boot 3.3.x, Java 17, Spring Security 6, Spring Data JPA, H2(mem)
* **인증**: `/auth/login`에서 **JWT 발급**, 이후 `Authorization: Bearer <token>`로 접근
* **인가**: `ROLE_USER`, `ROLE_ADMIN` 기반. 엔드포인트별 접근 제어 적용
* **예외**: 전역 `@RestControllerAdvice` + `401/403` JSON 핸들러
* **권한 보강(옵션)**: `AuthorityFilter`로 `X-Auth-Roles` 헤더 → 권한 합산/검증
* **개발 편의**: H2 콘솔 `/h2-console`, 시드 사용자 `user/password`, `admin/password`

---

## 프로젝트 구조

```
└── src
    ├── main
    │ ├── java
    │ │ └── com
    │ │     └── kyy
    │ │         └── springbootsecuritydemo
    │ │             ├── SpringbootSecurityDemoApplication.java
    │ │             ├── config
    │ │             │ └── BootstrapData.java
    │ │             ├── domain
    │ │             │ ├── entity
    │ │             │ │ └── UserAccount.java
    │ │             │ └── vo
    │ │             │     └── UserVo.java
    │ │             ├── error
    │ │             │ ├── ApiError.java
    │ │             │ ├── GlobalExceptionHandler.java
    │ │             │ └── entrypoint
    │ │             │     ├── RestAccessDeniedHandler.java
    │ │             │     └── RestAuthEntryPoint.java
    │ │             ├── repository
    │ │             │ └── UserAccountRepository.java
    │ │             └── security
    │ │                 ├── SecurityConfig.java
    │ │                 ├── controller
    │ │                 │ ├── AuthController.java
    │ │                 │ ├── ErrorDemoController.java
    │ │                 │ └── HelloController.java
    │ │                 ├── filter
    │ │                 │ ├── AuthorityFilter.java
    │ │                 │ └── JwtAuthenticationFilter.java
    │ │                 ├── jwt
    │ │                 │ └── JwtTokenProvider.java
    │ │                 └── service
    │ │                     └── JpaUserDetailsService.java
    │ └── resources
    │     ├── application.yaml
    │     ├── static
    │     └── templates
    └── test
        └── java
            └── com
                └── kyy
                    └── springbootsecuritydemo
                        └── SpringbootSecurityDemoApplicationTests.java

```

---

## 의존성

`pom.xml` 핵심(발췌):

```xml
<properties>
  <java.version>17</java.version>
  <spring-boot.version>3.3.2</spring-boot.version>
</properties>

<dependencies>
  <!-- Spring Boot 기본 -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
  </dependency>

  <!-- DB -->
  <dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
  </dependency>

  <!-- JWT -->
  <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
  </dependency>
  <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
  </dependency>

  <!-- Test -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

---

## 설정 (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate.format_sql: true
    open-in-view: false
  h2:
    console:
      enabled: true
      path: /h2-console

logging:
  level:
    org.hibernate.SQL: debug

app:
  jwt:
    # 최소 256비트(Base64) 권장. 운영에서 안전한 값으로 교체
    secret: "bXktdmVyeS1sb25nLXN1cGVyLXNlY3JldC1iYXNlNjQtMzJieXRlc2F0bGVhc3Q="
    expiration-minutes: 60
    issuer: "demo-auth"

# 404를 GlobalExceptionHandler로 보내고 싶다면 추가
# spring:
#   mvc:
#     throw-exception-if-no-handler-found: true
#   web:
#     resources:
#       add-mappings: false
```

---

## 도메인 & 인증

### UserAccount (JPA)

* `username`(unique), `password`(BCrypt), `roles`(`ROLE_` 접두 문자열)
* 권한은 `@ElementCollection<String>`로 간단 저장

### JpaUserDetailsService

* DB 사용자 → `UserDetails` 변환
* `roles` → `SimpleGrantedAuthority` 매핑

### JWT 구성

* **발급**: `JwtTokenProvider.generate(UserDetails)`
* **파싱**: `JwtTokenProvider.parse(token)`
* **필터**: `JwtAuthenticationFilter`가 `Authorization: Bearer ...` 검사 → `SecurityContext` 채움

### SecurityConfig (Stateless)

* `SessionCreationPolicy.STATELESS`
* 기본 권한 정책:

    * `permitAll`: `/auth/login`, `/api/public/**`, `/h2-console/**`
    * `ROLE_USER|ADMIN`: `/api/user/**`
    * `ROLE_ADMIN`: `/api/admin/**`
* `formLogin`/`httpBasic`는 **API 서버 기준 비활성 권장**
* 401/403을 JSON 고정: `RestAuthEntryPoint`, `RestAccessDeniedHandler`

---

## (옵션) AuthorityFilter

**헤더 기반 권한 보강/검증**이 필요할 때 사용.

* 헤더: `X-Auth-Roles: USER,ADMIN`
* 옵션:

    * `allowList`: 허용 가능한 권한 화이트리스트
    * `requireOnPaths`: 경로 패턴별 필수 권한(부족 시 403)
* 체인 연결:

    * **인증 이후 보강**: `addFilterAfter(authorityFilter(), UsernamePasswordAuthenticationFilter.class)`

> 운영에서는 **게이트웨이에서만 세팅되는 신뢰 헤더**로 한정(서명/HMAC 또는 mTLS 권장). 클라이언트 직접 지정 금지.

---

## 글로벌 예외 처리

* `@RestControllerAdvice`로 공통 포맷(`ApiError`) 제공
* 대표 핸들링:

    * `400`: 바인딩/유효성 오류(필드 상세 포함)
    * `401`: 인증 실패(EntryPoint)
    * `403`: 접근 거부(AccessDeniedHandler)
    * `404`: 미매핑(옵션 설정 필요)
    * `409`: 무결성 위반
    * `500`: 미처리 예외

예시(403):

```json
{
  "timestamp": "2025-08-10T01:23:45Z",
  "status": 403,
  "error": "Forbidden",
  "code": "ACCESS_DENIED",
  "message": "접근 권한이 없습니다.",
  "path": "/api/admin/ping"
}
```

---

## 빠른 시작

```bash
# 실행
mvn spring-boot:run
```

* **H2 콘솔**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

    * JDBC URL: `jdbc:h2:mem:testdb`
* **시드 사용자**(BootstrapData):

    * `user / password` → `ROLE_USER`
    * `admin / password` → `ROLE_ADMIN`

---

## 로그인 & 호출 예시

### 1) 로그인 → 토큰 발급

```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}'
```

응답 예:

```json
{
  "tokenType":"Bearer",
  "accessToken":"<JWT>",
  "username":"user",
  "roles":["ROLE_USER"]
}
```

### 2) 토큰으로 보호 API 접근

```bash
ACCESS_TOKEN="<JWT>"

# USER 권한
curl -H "Authorization: Bearer $ACCESS_TOKEN" \
  http://localhost:8080/api/user/ping

# ADMIN 권한 필요
curl -H "Authorization: Bearer $ACCESS_TOKEN" \
  http://localhost:8080/api/admin/ping
```

### 3) 어드민 토큰이 필요할 때

```bash
# admin/password로 로그인하여 발급
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'
```

---

## 설계 포인트 & 운영 팁

* **ROLE\_ 접두**: `hasRole("ADMIN")`는 내부적으로 `ROLE_ADMIN` 비교. 저장/클레임 모두 같은 규칙 유지.
* **비밀키 관리**: `app.jwt.secret`는 **최소 256bit Base64**. Vault/Parameter Store 사용 권장.
* **만료 전략**: Access 짧게(예: 15\~60분), **Refresh 토큰**은 별도 저장 & 회전(화이트/블랙리스트).
* **CORS**: SPA 호출이면 `CorsConfigurationSource`를 등록해 오리진/헤더/메서드/노출헤더를 명시.
* **H2/CSRF**: 데모용으로 CSRF 비활성. 폼 기반/쿠키 세션을 쓴다면 CSRF 전략 재활성 필요.
* **권한 캐시**: 대규모 환경에서 권한 변경 반영이 중요하면 토큰 짧게 + 서버측 권한 캐시/버전 클레임 고려.
* **로그/추적**: `ApiError.traceId`에 MDC(`X-Request-Id`) 연동하면 문제 추적이 쉬워짐.

---

## 다음 단계(옵션 확장)

* **Refresh 토큰 + 회전**: `/auth/refresh` 엔드포인트, 토큰 블랙리스트/화이트리스트
* **키 로테이션(JWK/RSA)**: `kid` 기반 공개키 노출, 키 교체 자동화
* **멀티 테넌트**: `tenantId`/`scopes` 클레임, 테넌트 경계별 인가 정책
* **권한 관리 고도화**: Role/Permission 테이블로 정규화, `@PreAuthorize` 스펙 도입
* **API 전용/웹 분리**: `/api/**`는 JSON 핸들러, 웹은 폼 로그인 리다이렉트로 분기

---

## 트러블슈팅 체크리스트

* 401이 뜨면:

    * `Authorization: Bearer <token>` 헤더 확인
    * 토큰 만료/서명 불일치 확인
* 403이 뜨면:

    * 토큰에 필요한 `ROLE_*` 포함 여부 확인
    * `SecurityConfig`의 경로 매핑/`AuthorityFilter` requireOnPaths 확인
* H2 콘솔 프레임 에러:

    * `headers().frameOptions().sameOrigin()` 설정 여부 확인

---

필요하면 **리프레시 토큰 + 키 로테이션**까지 포함한 운영형 구성으로 바로 확장해줄게.
