# Phase 3 — 보유 종목 / 수익률 조회

## 상태: 🔲 미시작

## Backend

**API**
- `GET /api/v1/accounts/{accountNo}/holdings` — 보유 종목 목록 (종목별 평가금액, 수익률)
- `GET /api/v1/portfolio/summary` — 전체 요약 (총 투자금액, 평가금액, 수익률, 종목 비중)

**구현 사항**
- [ ] 한투 잔고조회 API 호출 → 종목별 수익률 계산 로직
- [ ] 포트폴리오 요약 집계

## Mobile

- [ ] 포트폴리오 홈 화면
  - 총 수익률 표시
  - 보유 종목 리스트 (종목명, 보유수량, 수익률, 평가금액)
  - fl_chart 파이 차트 (종목 비중)
- [ ] `PortfolioNotifier` (Riverpod)
- [ ] pull-to-refresh, 로딩/에러 상태 처리
