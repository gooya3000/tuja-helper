# Phase 1 — 인증

## 상태: 🔲 미시작

## Backend

**엔티티**
- `User` (id, email, password, fcmToken, createdAt)

**API**
- `POST /api/v1/auth/signup` — 회원가입
- `POST /api/v1/auth/login` → Access Token (30분) + Refresh Token (7일) 발급
- `POST /api/v1/auth/refresh` — 토큰 갱신

**구현 사항**
- [ ] `User` 엔티티 + Repository
- [ ] 회원가입: 이메일 중복 검사, 비밀번호 BCrypt 암호화
- [ ] 로그인: JWT Access/Refresh 발급
- [ ] Refresh Token Redis 저장 (key: `refresh:{userId}`, TTL: 7일)
- [ ] Spring Security 필터 체인 구성 (JwtAuthenticationFilter)
- [ ] `SecurityConfig` — 인증 필요/불필요 엔드포인트 분리

## Mobile

- [ ] 로그인 화면 UI (`features/auth/presentation/login_screen.dart`)
- [ ] 회원가입 화면 UI (`features/auth/presentation/signup_screen.dart`)
- [ ] `AuthNotifier` (Riverpod) — 로그인 상태 관리
- [ ] flutter_secure_storage에 Access/Refresh Token 저장
- [ ] 앱 시작 시 자동 로그인 (토큰 유효성 확인)
- [ ] go_router 인증 가드 — 비로그인 시 `/login` 리다이렉트
