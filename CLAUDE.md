# tuja-helper

한국투자증권 OpenAPI 기반 주식투자 보조 모바일 앱.
Backend (Kotlin + Spring Boot 3.x) + Mobile (Flutter) 동시 개발.

## 기술 스택

| 항목 | 결정 |
|------|------|
| DB | PostgreSQL (로컬: Docker Compose) |
| Cache | Redis (로컬: Docker Compose) |
| 인증 | JWT — Access 30분 / Refresh 7일 (Redis 저장) |
| Flutter 상태관리 | Riverpod (riverpod_generator) |
| Flutter 라우팅 | go_router |
| 인프라 | 로컬 우선 → 안정화 후 AWS 마이그레이션 |
| 한투 API 환경 | 모의투자 (`openapivts.koreainvestment.com`) |

## 로컬 실행

```bash
docker compose up -d                    # DB + Redis
cd backend && ./gradlew bootRun         # http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
cd mobile && flutter pub get && flutter run
```

## 개발 현황

| Phase | 내용 | 상태 | 상세 |
|-------|------|------|------|
| 0 | 프로젝트 초기 세팅 | ✅ 완료 | [phase0-setup.md](docs/phases/phase0-setup.md) |
| 1 | 인증 (JWT) | ✅ 완료 | [phase1-auth.md](docs/phases/phase1-auth.md) |
| 2 | 한투 API 연동 (계좌/잔고) | 🔲 미시작 | [phase2-kis-api.md](docs/phases/phase2-kis-api.md) |
| 3 | 보유 종목 / 수익률 | 🔲 미시작 | [phase3-portfolio.md](docs/phases/phase3-portfolio.md) |
| 4 | 시세 조회 + 알림 | 🔲 미시작 | [phase4-quote-alert.md](docs/phases/phase4-quote-alert.md) |

## 참고 문서

- [기획서](docs/plan.md)
- [개발 환경 설정](docs/dev-guide.md)
