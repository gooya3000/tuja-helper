# 개발 환경 설정 가이드

## 사전 준비

- JDK 21
- Docker Desktop
- Flutter 3.22+
- IntelliJ IDEA (Backend) / VS Code or Android Studio (Mobile)

---

## 로컬 환경 시작

### 1. DB / Redis 실행

```bash
# 프로젝트 루트에서
docker compose up -d
```

### 2. 환경변수 설정 (Backend)

`backend/.env` 파일 생성 (또는 IDE Run Configuration에 등록):

```
DB_USERNAME=tujahelper
DB_PASSWORD=tujahelper
JWT_SECRET=local-dev-secret-key-must-be-at-least-256-bits-long
ENCRYPTION_KEY=local-dev-aes-key-32bytes!!
```

### 3. Backend 실행

```bash
cd backend
./gradlew bootRun
```

Swagger UI: http://localhost:8080/swagger-ui.html

### 4. Mobile 실행

```bash
cd mobile
flutter pub get
flutter run
```

---

## 한국투자증권 API 설정

- 모의투자 신청: https://apiportal.koreainvestment.com
- Base URL (모의): `https://openapivts.koreainvestment.com:29443`
- App Key / App Secret 발급 후 앱에서 등록

---

## 브랜치 전략

```
main          ← 배포 브랜치
develop       ← 통합 브랜치
feature/xxx   ← 기능 개발 브랜치
```

## API 에러 코드 규칙

| 코드 | 의미 |
|------|------|
| AUTH_001 | 인증 토큰 없음/만료 |
| AUTH_002 | 권한 없음 |
| USER_001 | 이메일 중복 |
| KIS_001  | 한투 API 오류 |
| VALIDATION_ERROR | 입력값 오류 |
