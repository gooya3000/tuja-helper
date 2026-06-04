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
> 구현 시 Backend 에이전트가 이 섹션을 채운다

## Phase 3 — 보유 종목 / 수익률
> 구현 시 Backend 에이전트가 이 섹션을 채운다

## Phase 4 — 시세 조회 + 알림
> 구현 시 Backend 에이전트가 이 섹션을 채운다
