# Phase 0 — 프로젝트 초기 세팅

## 상태: ✅ 완료

## 완료 항목

**Backend**
- [x] Spring Boot 3.x + Kotlin 프로젝트 구조 생성 (`build.gradle.kts`, `settings.gradle.kts`)
- [x] `application.yml` — DB / Redis / JWT / 암호화 / 한투 API 설정
- [x] `TujaHelperApplication.kt` — 진입점 (`@EnableScheduling` 포함)
- [x] `common/ApiResponse.kt` — 공통 응답 포맷
- [x] `common/TujaException.kt` — 커스텀 예외 + 팩토리 메서드
- [x] `common/GlobalExceptionHandler.kt` — 전역 예외 처리

**Mobile**
- [x] Flutter 프로젝트 구조 생성 (`pubspec.yaml`)
- [x] `main.dart` — ProviderScope + Firebase 초기화
- [x] `core/router/app_router.dart` — go_router 기본 설정
- [x] `core/theme/app_theme.dart` — Material 3 라이트/다크 테마
- [x] `shared/network/api_client.dart` — Dio + JWT interceptor (401 자동 토큰 갱신)

**인프라**
- [x] `docker-compose.yml` — PostgreSQL 16 + Redis 7 로컬 환경
- [x] `.gitignore`
