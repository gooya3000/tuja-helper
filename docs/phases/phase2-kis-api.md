# Phase 2 — 한투 API 연동 (계좌/잔고)

## 상태: 🔲 미시작

## Backend

**엔티티**
- `BrokerageCredential` (id, userId, appKey, appSecret, accountNo — appKey/appSecret AES-256 암호화)

**API**
- `POST /api/v1/brokerage/credentials` — API Key 등록
- `GET /api/v1/accounts` — 계좌 목록 조회
- `GET /api/v1/accounts/{accountNo}/balance` — 잔고 조회

**구현 사항**
- [ ] `BrokerageCredential` 엔티티 + 암호화/복호화 로직 (AES-256, 키는 `app.encryption.key`)
- [ ] 한투 OAuth 토큰 발급 (`WebClient` → `POST /oauth2/tokenP`)
- [ ] 발급된 한투 토큰 Redis 캐싱 (key: `kis:token:{userId}`, TTL = 만료시간 기준)
- [ ] 한투 REST API 호출 공통 클라이언트 (`KisApiClient`)

## Mobile

- [ ] API Key 입력/등록 화면
- [ ] `AccountNotifier` (Riverpod) — 계좌 목록 상태 관리
- [ ] 잔고 현황 화면 (총 평가금액, 예수금)
