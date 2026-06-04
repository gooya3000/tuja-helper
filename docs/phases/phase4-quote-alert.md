# Phase 4 — 시세 조회 + 알림

## 상태: 🔲 미시작

## Backend

**엔티티**
- `Alert` (id, userId, stockCode, targetPrice, direction[UP/DOWN], isActive)
- `Notification` (id, userId, alertId, sentAt) — 발송 이력

**API**
- `GET /api/v1/quotes/{stockCode}` — 현재가 조회 (Redis 캐싱 TTL 10초)
- `GET /api/v1/quotes/search?keyword=` — 종목 검색
- `POST /api/v1/alerts` — 알림 등록
- `GET /api/v1/alerts` — 알림 목록
- `DELETE /api/v1/alerts/{alertId}` — 알림 삭제

**구현 사항**
- [ ] 시세 조회 + Redis 캐싱 (TTL 10초)
- [ ] `Alert` / `Notification` 엔티티 + Repository
- [ ] Spring Scheduler: 30초마다 활성 알림 종목 시세 조회 → 조건 충족 시 FCM 발송
- [ ] FCM 발송 서비스 (`NotificationService`)

## Mobile

- [ ] 종목 검색 화면
- [ ] 종목 상세 화면 (현재가, 등락률, fl_chart 라인 차트)
- [ ] 알림 설정 화면 (목표가/손절가 입력)
- [ ] FCM 수신 처리 (백그라운드/포그라운드)
- [ ] `AlertNotifier` (Riverpod)
