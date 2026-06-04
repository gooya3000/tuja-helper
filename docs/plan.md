# 투자 보조 모바일 앱 기획 및 개발 문서

## 1. 프로젝트 개요

### 1.1 프로젝트명

Stock Assistant App

### 1.2 목적

한국투자증권 OpenAPI를 활용하여 다중 사용자가 자신의 주식 계좌를 연동하고, 보유 종목/관심 종목/시세/수익률/알림/주문 기능을 사용할 수 있는 모바일 투자 보조 서비스를 개발한다.

서버는 Kotlin + Spring Boot 기반으로 구현하고, 모바일 앱은 Flutter 기반으로 iOS/Android 양쪽을 지원한다.

### 1.3 핵심 목표

1. 사용자가 앱에서 자신의 투자 현황을 한눈에 확인할 수 있다.
2. 한국투자증권 OpenAPI와 연동하여 계좌, 잔고, 시세, 주문 정보를 조회할 수 있다.
3. 실시간 시세 또는 주기적 시세 조회를 기반으로 목표가/손절가 알림을 제공한다.
4. 사용자의 API Key, 계좌 정보, 토큰 정보는 모바일 앱에 저장하지 않고 서버에서 안전하게 관리한다.
5. 다중 사용자 서비스를 전제로 인증, 권한, 암호화, 주문 검증, 감사 로그를 설계한다.

---

## 2. 기술 스택

### 2.1 Backend

- Language: Kotlin
- Framework: Spring Boot
- Build Tool: Gradle Kotlin DSL
- ORM: Spring Data JPA
- DB: PostgreSQL 또는 MySQL
- Cache: Redis
- Security: Spring Security + JWT
- Batch/Scheduler: Spring Scheduler 또는 Quartz
- HTTP Client: WebClient
- WebSocket Client: 한국투자증권 실시간 시세 연동용
- API Docs: OpenAPI/Swagger
- Test: JUnit5, MockK, Kotest 선택 가능

### 2.2 Mobile App

- Framework: Flutter
- Language: Dart
- State Management: Riverpod 또는 Bloc
- Local Storage: Flutter Secure Storage
- Push Notification: Firebase Cloud Messaging
- Chart: fl_chart 또는 syncfusion_flutter_charts
- HTTP Client: Dio
- WebSocket Client: 필요 시 앱-서버 간 실시간 데이터 수신용

### 2.3 Infra

- Cloud: AWS 기준으로 설계
- Compute: ECS Fargate 또는 EC2
- DB: RDS
- Cache: ElastiCache Redis
- Secret: AWS Secrets Manager 또는 Parameter Store
- Monitoring: CloudWatch + Sentry
- CI/CD: GitHub Actions
- Container: Docker

---

## 3. 전체 아키텍처

```text
Flutter App
  ↓
Backend API Server - Kotlin Spring Boot
  ↓
Internal Services
  ├─ Auth Service
  ├─ User Service
  ├─ Brokerage Connection Service
  ├─ Account Service
  ├─ Stock Quote Service
  ├─ Order Service
  ├─ Watchlist Service
  ├─ Alert Service
  ├─ Portfolio Service
  └─ Notification Service
  ↓
External API
  └─ 한국투자증권 OpenAPI
        ├─ REST API
        └─ WebSocket API