# API Contract

Backend 에이전트가 API를 구현할 때 이 문서에 스펙을 기록한다.
Frontend 에이전트와 QA 에이전트는 이 문서를 읽고 연동 및 테스트를 작성한다.

**Base URL:** `http://localhost:8080/api/v1`

**공통 응답 형식:**
```json
{ "success": true, "data": { ... } }
{ "success": false, "error": { "code": "ERROR_CODE", "message": "설명" } }
```

---

## Phase 1 — 인증

### POST /auth/signup
회원가입

**Request**
```json
{
  "email": "string",
  "password": "string (8자 이상)"
}
```

**Response 201**
```json
{
  "success": true,
  "data": { "userId": "long" }
}
```

**Error Cases**
| code | message | HTTP |
|------|---------|------|
| `DUPLICATE_EMAIL` | 이미 사용 중인 이메일입니다 | 409 |
| `INVALID_INPUT` | 입력값이 올바르지 않습니다 | 400 |

---

### POST /auth/login
로그인 — Access Token + Refresh Token 발급

**Request**
```json
{
  "email": "string",
  "password": "string"
}
```

**Response 200**
```json
{
  "success": true,
  "data": {
    "accessToken": "string (JWT, 30분)",
    "refreshToken": "string (JWT, 7일)"
  }
}
```

**Error Cases**
| code | message | HTTP |
|------|---------|------|
| `INVALID_CREDENTIALS` | 이메일 또는 비밀번호가 올바르지 않습니다 | 401 |

---

### POST /auth/refresh
Access Token 갱신

**Request**
```json
{
  "refreshToken": "string"
}
```

**Response 200**
```json
{
  "success": true,
  "data": {
    "accessToken": "string (JWT, 30분)"
  }
}
```

**Error Cases**
| code | message | HTTP |
|------|---------|------|
| `INVALID_TOKEN` | 유효하지 않은 토큰입니다 | 401 |
| `EXPIRED_TOKEN` | 만료된 토큰입니다 | 401 |

---

## Phase 2 — 한투 API 연동

### POST /brokerage/credentials
한국투자증권 API Key/Secret/계좌번호 등록 (JWT 인증 필요)

**Request**
```json
{
  "appKey": "string (필수)",
  "appSecret": "string (필수)",
  "accountNo": "string (필수)"
}
```

**Response 200**
```json
{
  "success": true
}
```

**Error Cases**
| code | message | HTTP |
|------|---------|------|
| `INVALID_INPUT` | appKey는 필수입니다 | 400 |
| `UNAUTHORIZED` | 인증이 필요합니다 | 401 |

**구현 상세**
- appKey, appSecret은 AES-256 CBC 모드로 암호화하여 DB 저장 (IV는 암호문에 prefix)
- 이미 등록된 경우 덮어쓰기(업데이트)

---

### GET /accounts
계좌 목록 조회 (JWT 인증 필요)

**Response 200**
```json
{
  "success": true,
  "data": [
    {
      "accountNo": "string",
      "accountName": "string"
    }
  ]
}
```

**Error Cases**
| code | message | HTTP |
|------|---------|------|
| `CREDENTIALS_NOT_FOUND` | API 키가 등록되어 있지 않습니다 | 404 |
| `UNAUTHORIZED` | 인증이 필요합니다 | 401 |

---

### GET /accounts/{accountNo}/balance
잔고 조회 (JWT 인증 필요)

**Path Variable**
- `accountNo`: 계좌번호

**Response 200**
```json
{
  "success": true,
  "data": {
    "totalEvaluationAmount": "number (총평가금액)",
    "depositAmount": "number (예수금)",
    "totalProfitLossAmount": "number (평가손익합계)",
    "totalProfitLossRate": "number (수익률)"
  }
}
```

**Error Cases**
| code | message | HTTP |
|------|---------|------|
| `CREDENTIALS_NOT_FOUND` | API 키가 등록되어 있지 않습니다 | 404 |
| `UNAUTHORIZED` | 인증이 필요합니다 | 401 |

**구현 상세**
- 한투 OAuth 토큰은 Redis에 `kis:token:{userId}` 키로 캐싱 (TTL = 토큰 만료시간)
- 캐시 미스 시 `POST /oauth2/tokenP`로 토큰 재발급
- 한투 모의투자 환경: `openapivts.koreainvestment.com:29443`

## Phase 3 — 보유 종목 / 수익률
> 구현 시 Backend 에이전트가 이 섹션을 채운다

## Phase 4 — 시세 조회 + 알림
> 구현 시 Backend 에이전트가 이 섹션을 채운다
