다음 TDD 워크플로우로 Phase $ARGUMENTS 개발을 시작합니다.

## 실행 전 준비

아래 두 파일을 반드시 먼저 읽어 요구사항과 API 스펙을 파악하라:
- `docs/phases/phase$ARGUMENTS-*.md`
- `docs/api-contract.md`

---

## Step 1 — Red (qa 에이전트)

qa 에이전트를 `isolation: worktree`로 호출한다.
- 브랜치명: `phase$ARGUMENTS/test`
- 작업 지시: "Phase $ARGUMENTS 명세(`docs/phases/phase$ARGUMENTS-*.md`)와 `docs/api-contract.md`를 읽고, backend와 frontend 실패 테스트를 각각 작성하라. 아직 구현하지 않는다."

qa 에이전트가 완료되면 Step 2로 진행한다.

---

## Step 2 — Green (backend + frontend 에이전트 병렬)

backend 에이전트와 frontend 에이전트를 **동시에** `isolation: worktree`로 호출한다.

**backend 에이전트 지시:**
- 브랜치명: `phase$ARGUMENTS/backend`
- "Step 1에서 작성된 backend 테스트(`backend/src/test/`)를 통과시키는 구현을 작성하라. 완료 후 `docs/api-contract.md`에 실제 구현된 스펙을 반영하라."

**frontend 에이전트 지시:**
- 브랜치명: `phase$ARGUMENTS/frontend`
- "Step 1에서 작성된 frontend 테스트(`mobile/test/`)를 통과시키는 구현을 작성하라. `docs/api-contract.md`의 API 스펙을 기준으로 연동하라."

두 에이전트 모두 완료되면 Step 3으로 진행한다.

---

## Step 3 — Refactor (qa 에이전트, 최대 2회 반복)

qa 에이전트를 호출한다 (worktree 불필요).
- 작업 지시: "Phase $ARGUMENTS 구현이 완료됐다. backend는 `./gradlew test`, frontend는 `flutter test`를 실행하여 전체 통과 여부를 확인하라. 커버리지 공백과 `docs/api-contract.md` 불일치도 리포트하라."

### 실패 시 재시도 규칙

테스트가 실패하면 아래 순서로 최대 2회 재시도한다:

**1회차 재시도 전:**
- 실패한 테스트 목록과 원인을 사용자에게 보고한다
- "재시도할까요?" 라고 물어 사용자 승인을 받은 후 진행한다
- 승인 시: 실패한 영역(backend/frontend)의 에이전트를 해당 worktree 브랜치에서 다시 호출해 수정
- 수정 완료 후 qa 에이전트로 테스트 재실행
- 전체 통과 시 완료

**2회차 재시도 전 (1회차 후에도 실패 시):**
- 동일하게 실패 내용을 보고하고 사용자 승인을 받은 후 진행한다
- 승인 시: 실패 에이전트 재호출 후 수정
- 수정 완료 후 qa 에이전트로 테스트 재실행
- 전체 통과 시 완료

**2회 모두 실패 시:**
- 자동 재시도를 중단하고 사용자에게 실패 원인과 내용을 상세히 리포트한다
- 추가 진행 여부는 사용자가 결정한다

---

## 전체 완료 기준

- `./gradlew test` 전체 통과
- `flutter test` 전체 통과
- `docs/api-contract.md` 최신 상태 반영
- 각 브랜치(`phase$ARGUMENTS/test`, `phase$ARGUMENTS/backend`, `phase$ARGUMENTS/frontend`) 커밋 완료

## 마지막 단계 (위 완료 기준 충족 후 반드시 실행)

아래 명령어를 Bash 툴로 실행한다:

```bash
echo "$ARGUMENTS" > /tmp/tuja_phase_complete
```
