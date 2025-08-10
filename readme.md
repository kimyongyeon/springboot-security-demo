# Spring Boot Security JWT 데모

JWT 인증, JPA 사용자 관리, 역할 기반 권한 부여를 포함한 현대적인 보안 패턴을 시연하는 포괄적인 Spring Boot 애플리케이션입니다.

## 🚀 주요 기능

- **JWT 인증**: JSON Web Token을 활용한 상태 비저장 인증
- **역할 기반 권한 부여**: 사용자 및 관리자 역할 관리
- **JPA 통합**: Spring Data JPA와 H2 인메모리 데이터베이스
- **전역 예외 처리**: JSON 응답을 포함한 중앙 집중식 오류 처리
- **보안 필터**: 커스텀 JWT 인증 및 권한 필터
- **H2 콘솔**: 개발용 데이터베이스 콘솔 접근
- **포괄적인 보안**: 401/403 JSON 핸들러, CORS 지원

## 🛠 기술 스택

- **Spring Boot** 3.5.4
- **Java** 17
- **Spring Security** 6
- **Spring Data JPA**
- **H2 Database** (인메모리)
- **JWT** (JJWT 0.11.5)
- **Lombok**
- **Gradle**

## 📁 프로젝트 구조

```
src/main/java/com/kyy/springbootsecuritydemo/
├── SpringbootSecurityDemoApplication.java    # 메인 애플리케이션
├── common/
│   ├── config/
│   │   └── BootstrapData.java                 # 초기 데이터 시딩
│   ├── entrypoint/
│   │   ├── PublicEndpoints.java               # 공개 API 엔드포인트
│   │   ├── RestAccessDeniedHandler.java       # 403 핸들러
│   │   └── RestAuthEntryPoint.java            # 401 핸들러
│   ├── error/
│   │   ├── ApiError.java                      # 오류 응답 모델
│   │   └── GlobalExceptionHandler.java       # 전역 예외 핸들러
│   └── security/
│       ├── SecurityConfig.java                # 보안 설정
│       ├── controller/
│       │   ├── AuthController.java            # 인증 엔드포인트
│       │   ├── ErrorDemoController.java       # 오류 시연용
│       │   └── HelloController.java           # 보호된 엔드포인트
│       ├── domain/
│       │   ├── entity/UserAccount.java        # 사용자 엔티티
│       │   └── vo/UserVo.java                 # 사용자 값 객체
│       ├── filter/
│       │   ├── AuthorityFilter.java           # 권한 강화 필터
│       │   └── JwtAuthenticationFilter.java   # JWT 인증 필터
│       ├── jwt/
│       │   └── JwtTokenProvider.java          # JWT 토큰 관리
│       ├── repository/
│       │   └── UserAccountRepository.java     # 사용자 리포지토리
│       └── service/
│           └── JpaUserDetailsService.java     # 사용자 세부정보 서비스
└── work/
    └── controller/
        └── HiController.java                  # 추가 데모 컨트롤러
```

## 🔧 설정

### 애플리케이션 설정 (`application.yaml`)

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

app:
  jwt:
    secret: "bXktdmVyeS1sb25nLXN1cGVyLXNlY3JldC1iYXNlNjQtMzJieXRlc2F0bGVhc3Q="
    expiration-minutes: 60
    issuer: "demo-auth"
```

### 의존성 (`build.gradle`)

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    
    runtimeOnly 'com.h2database:h2'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}
```

## 🚀 빠른 시작

### 1. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 2. 접근점

- **애플리케이션**: http://localhost:8080
- **H2 콘솔**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - 사용자명: `sa`
  - 비밀번호: (비어있음)

### 3. 기본 사용자

애플리케이션 시작 시 기본 사용자가 생성됩니다:

- **사용자**: `user` / `password` (ROLE_USER)
- **관리자**: `admin` / `password` (ROLE_ADMIN)

## 🔐 인증 및 권한 부여

### 로그인 및 JWT 토큰 발급

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}'
```

**응답:**
```json
{
  "tokenType": "Bearer",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "user",
  "roles": ["ROLE_USER"]
}
```

### 보호된 엔드포인트에서 JWT 토큰 사용

```bash
# 토큰 설정
TOKEN="your_jwt_token_here"

# 사용자 엔드포인트 접근 (ROLE_USER 또는 ROLE_ADMIN 필요)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/user/ping

# 관리자 엔드포인트 접근 (ROLE_ADMIN만 허용)
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/ping
```

## 🛡️ 보안 기능

### 엔드포인트 보호

- **공개**: `/auth/login`, `/api/public/**`, `/h2-console/**`
- **사용자 보호**: `/api/user/**` (ROLE_USER 또는 ROLE_ADMIN)
- **관리자 전용**: `/api/admin/**` (ROLE_ADMIN만)

### 오류 처리

애플리케이션은 일관된 JSON 오류 응답을 제공합니다:

```json
{
  "timestamp": "2025-08-10T01:23:45Z",
  "status": 403,
  "error": "Forbidden",
  "code": "ACCESS_DENIED",
  "message": "접근이 거부되었습니다",
  "path": "/api/admin/ping"
}
```

### 권한 필터 (선택사항)

`AuthorityFilter`는 헤더를 기반으로 사용자 권한을 강화할 수 있습니다:
- 헤더: `X-Auth-Roles: USER,ADMIN`
- 게이트웨이 레벨에서 권한을 주입하는 마이크로서비스 아키텍처에 유용

## 🧪 테스트

### 테스트 실행

```bash
./gradlew test
```

### 엔드포인트 테스트

```bash
# 공개 엔드포인트
curl http://localhost:8080/api/public/hello

# 오류 시연
curl http://localhost:8080/error/demo/400
curl http://localhost:8080/error/demo/500
```

## 📊 데이터베이스 스키마

### UserAccount 엔티티

```sql
CREATE TABLE user_account (
    id BIGINT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE
);

CREATE TABLE user_account_roles (
    user_account_id BIGINT,
    roles VARCHAR(255)
);
```

## 🔧 개발 팁

### JWT 시크릿 설정

운영 환경에서는 안전한 256비트 Base64 인코딩된 시크릿을 사용하세요:
```bash
# 안전한 시크릿 생성
openssl rand -base64 32
```

### CORS 설정

프론트엔드 통합을 위해 `SecurityConfig`에서 CORS를 설정하세요:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

## 🚨 보안 고려사항

- **JWT 시크릿**: 운영 환경에서는 환경 변수 사용
- **토큰 만료**: 운영 환경에서는 더 짧은 만료 시간 고려
- **리프레시 토큰**: 보안 향상을 위한 리프레시 토큰 메커니즘 구현
- **HTTPS 전용**: 운영 환경에서는 항상 HTTPS 사용
- **입력 검증**: 모든 사용자 입력에 대한 검증
- **속도 제한**: 인증 엔드포인트에 대한 속도 제한 구현

## 🔮 향후 계획

- [ ] 리프레시 토큰 메커니즘 구현
- [ ] 비밀번호 재설정 기능 추가
- [ ] 로그인 실패 후 계정 잠금 구현
- [ ] API 버전 관리 추가
- [ ] 감사 로깅 구현
- [ ] 통합 테스트 추가
- [ ] Docker 지원 추가
- [ ] OAuth2 통합 구현

## 🐛 문제 해결

### 일반적인 문제들

**401 Unauthorized (인증되지 않음)**
- Authorization 헤더에 JWT 토큰이 포함되어 있는지 확인
- 토큰이 만료되지 않았는지 확인
- 토큰 형식이 올바른지 확인: `Bearer <token>`

**403 Forbidden (접근 금지)**
- 사용자가 해당 엔드포인트에 필요한 역할을 가지고 있는지 확인
- SecurityConfig 경로 매핑 확인
- 역할이 `ROLE_` 접두사를 가지고 있는지 확인

**H2 콘솔 접근 문제**
- `/h2-console/**`이 허용된 경로에 있는지 확인
- 프레임 옵션 설정 확인
- application.yaml에서 H2 콘솔이 활성화되어 있는지 확인