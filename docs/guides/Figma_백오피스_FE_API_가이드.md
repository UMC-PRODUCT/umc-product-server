# Figma 백오피스 FE API 가이드

> 작성일: 2026-05-08
>
> 본 문서는 `feature/#836-llm-classifier` 브랜치에서 개발된 Figma admin API 를 웹 백오피스에서 사용할 수 있도록 정리한 FE 구현 가이드다. 다음 항목을 다룬다.
>
> - 4 가지 use case flow 와 각 flow 가 호출해야 하는 endpoint 시퀀스
> - 모든 endpoint 의 request / response 스펙
> - 에러 코드 / 권한 정책
> - 백오피스 화면 구성 instruction (정보 구조 · 컴포넌트 · 인터랙션)

---

## 0. 공통 사항

### 0.1 Base URL & 인증

- 모든 endpoint 의 prefix 는 `/api/v1/admin/figma/...` 다.
- **모든 호출은 `Authorization: Bearer {accessToken}` 헤더 필수** (단 1 개 예외 — `GET /oauth/callback` 은 `@Public`. FE 가 직접 호출하지 않으므로 무시해도 된다).
- 호출 주체는 **`SUPER_ADMIN` 단일 역할만 통과**한다 (ADR-007). 그 외 역할이면 모든 endpoint 가 `RESOURCE_ACCESS_DENIED` 를 반환한다. 즉 백오피스 메뉴 자체를 SUPER_ADMIN 외에는 보여주지 않거나, 진입 시 권한을 미리 확인해야 한다.

### 0.2 응답 래핑 (`ApiResponse`)

[ApiResponse.java](../../src/main/java/com/umc/product/global/response/ApiResponse.java) 에 의해 모든 응답이 다음 형태로 래핑된다.

**성공 응답:**

```json
{
    "success": true,
    "code": "COMMON-200",
    "message": "성공입니다.",
    "result": <컨트롤러가
    리턴한
    값>
}
```

**실패 응답:**

```json
{
    "success": false,
    "code": "FIGMA-0006",
    "message": "등록된 Figma 폴링 대상 파일이 아닙니다.",
    "result": null
}
```

> 본 문서의 "Response" 섹션에는 **`result` 안에 들어가는 값만** 기술한다. FE 에서는 항상 `response.result` 로 접근.

### 0.3 주요 Enum

| Enum                 | 값              | 의미                                             |
|----------------------|----------------|------------------------------------------------|
| `DiscordMentionType` | `ROLE`, `USER` | Discord 멘션이 role(`<@&id>`) 인지 user(`<@id>`) 인지 |

### 0.4 도메인 모델 한 줄 정의

| 객체                 | 정의                                                      | 핵심 필드                                                              |
|--------------------|---------------------------------------------------------|--------------------------------------------------------------------|
| **Integration**    | Figma OAuth 위임 결과. owner 운영진의 access/refresh token 을 보관 | `id`, `ownerMemberId`                                              |
| **Watched File**   | 폴링 대상 Figma 파일 1 건                                      | `fileKey`, `displayName`, `enabled`, `lastSyncedAt`, `lastError`   |
| **Routing Domain** | LLM 분류 결과 키(`domain_key`) 가 매칭될 라우팅 단위                  | `domainKey`, `discordWebhookUrl`, `fallback`                       |
| **Mention**        | 라우팅 도메인이 Discord 알림 시 멘션할 대상                            | `mentionId`, `mentionType`, `displayLabel`                         |
| **Comment**        | 시간창 안의 Figma 댓글. `classifiedDomainKey` 로 어디로 묶일지 결정     | `commentId`, `message`, `classifiedDomainKey`, `alreadyDispatched` |

---

## 1. Use Case 별 Flow

### Flow A — 최초 셋업 (운영진 1 회성)

> 처음 백오피스에 들어와 Figma → Discord 포워딩을 활성화할 때의 흐름. 순서가 중요하다.

```
1. OAuth 위임   →  GET  /oauth                     (authorize URL 발급)
                    Figma 동의 화면으로 redirect
                    GET  /oauth/callback             (자동, FE 가 만지지 않음)
2. 라우팅 도메인 → POST /routing-domains             (각 도메인 키마다 1 회, fallback=true 1 건 필수)
3. 멘션 등록    →  POST /routing-domains/{id}/mentions  (각 도메인에 N 명)
4. Watched 파일 → POST /watched-files               (Figma 파일별 1 회)
5. preview 검증 → GET  /preview?watchedFileId=...   (실제 발송 전 시뮬레이션)
6. 즉시 sync 1회 → POST /sync                       (정기 스케줄 도래 전 한 번 비우기)
```

**중요 — fallback 도메인이 없으면 분류 매칭이 실패한 댓글이 사라진다.** 라우팅 도메인 등록 단계에서 반드시 `fallback: true` 인 도메인을 1 건 만든다 (ADR-003).

### Flow B — 일상 모니터링 (운영진 매주)

> 발송 누락이 없는지, 라우팅이 의도대로 동작하는지 확인하는 흐름.

```
1. Watched 파일 상태 → GET /watched-files
                       각 row 의 lastSyncedAt / lastError 확인
2. 라우팅 도메인 점검 → GET /routing-domains
                       fallback 1 건 / mentionCount > 0 인지 확인
3. (이상 발견 시)     → GET /preview?from=...&to=...
                       어떤 댓글이 어디로 분류되었는지 시각 검증
                       alreadyDispatched=false 인 댓글이 다음 sync 에서 발송될 후보
```

### Flow C — 수동 트리거 (운영진 ad-hoc)

> 정기 스케줄을 기다리지 않거나, 과거 시간창을 다시 발송할 때의 흐름.

| 상황                       | 호출                                              | 부수효과                                                                  |
|--------------------------|-------------------------------------------------|-----------------------------------------------------------------------|
| "방금 단 댓글 빨리 보내고 싶다"      | `POST /sync` 또는 `POST /sync/watched-files/{id}` | Discord 발송 ✓ / dispatch 기록 ✓ / cursor 갱신 ✓ (멱등)                       |
| "지난주 누락된 댓글 catch-up 발송" | `POST /digest?from=...&to=...`                  | Discord 발송 ✓ / dispatch 기록 ✗ / cursor 갱신 ✗ (비-멱등, 같은 호출 두 번 = 두 번 발송) |
| "발송 전에 분류 결과만 미리 보기"     | `GET /preview?from=...&to=...`                  | 발송 ✗ / 상태 변경 ✗                                                        |

> `digest` 는 **비-멱등** 이라 운영자가 의도한 catch-up 외에는 절대 두 번 누르면 안 된다. UI 에 빨간색 경고 + confirm 모달을 깐다.

### Flow D — 운영 매핑 변경 (운영진 가끔)

> 새 도메인 추가, 담당자 교체, 파일 추가/제거 등.

```
도메인 변경:
  POST   /routing-domains                         (신규 키 추가)
  PATCH  /routing-domains/{id}                    (설명 · webhook URL · fallback 수정)
  POST   /routing-domains/{id}/mentions           (멘션 추가)
  PATCH  /routing-domains/mentions/{mentionId}    (멘션 Discord ID · 라벨 수정)
  DELETE /routing-domains/mentions/{mentionId}    (멘션 제거)
  DELETE /routing-domains/{domainId}              (도메인 삭제 — mention 도 cascade)

파일 변경:
  POST   /watched-files                           (신규 파일)
  DELETE /watched-files/{id}                      (비활성화 — 데이터는 보존)
  POST   /watched-files/{id}/enable               (재활성화)
```

> `domain_key` 는 LLM 분류 키이므로 수정 불가. 변경이 필요하면 삭제 후 재등록한다. `mentionType` (ROLE/USER) 도 동일하게 삭제 후 재등록으로만 변경 가능하다.

---

## 2. API 상세 스펙

### 2.1 OAuth — Figma 위임 인증

#### `GET /api/v1/admin/figma/oauth` — `[FIGMA-001]` authorize URL 발급

권한: `FIGMA.MANAGE`

**Request**: 없음 (Authorization 헤더만)

**Response**:

```json
{
    "authorizeUrl": "https://www.figma.com/oauth?client_id=...&state=...&...",
    "state": "abc123def456..."
}
```

FE 동작: 응답을 받으면 `window.location.href = authorizeUrl` 로 리다이렉트한다. `state` 는 callback 까지 자동으로 흐르므로 FE 에서 별도로 보관하지 않아도 된다 (서버 세션에 묶임).

#### `GET /api/v1/admin/figma/oauth/callback` — `[FIGMA-002]`

권한: 없음 (`@Public`, JWT 미포함 redirect 라서)

FE 가 **직접 호출하지 않는다**. Figma 동의 화면에서 자동 redirect 된다. 백엔드가 처리 후 응답을 반환하지만 일반적으로 운영자가 보는 것은 백엔드가 별도 frontend redirect URL 로 다시 보낸 화면이다.

### 2.2 Watched Files — 폴링 대상 파일

#### `POST /api/v1/admin/figma/watched-files` — `[FIGMA-003]` 등록

권한: `FIGMA.MANAGE`

**Request**:

```json
{
    "fileKey": "abcDEF12345",
    "displayName": "디자인 시스템 v3"
}
```

| 필드            | 타입     | 제약        | 설명                                      |
|---------------|--------|-----------|-----------------------------------------|
| `fileKey`     | string | 필수, 1~100 | Figma 파일 URL 의 `/file/{fileKey}/...` 부분 |
| `displayName` | string | 필수, 1~255 | 백오피스에서 식별할 이름                           |

**Response**: `{ "watchedFileId": 17 }`

**에러**: `FIGMA-0007` (이미 등록된 파일 키)

#### `DELETE /api/v1/admin/figma/watched-files/{watchedFileId}` — `[FIGMA-004]` 비활성화

권한: `FIGMA.MANAGE`

비활성화는 **soft disable** 다. 데이터는 보존되며 `enabled=false` 로 바뀌어 폴링 대상에서 제외된다.

**Response**: 없음 (200 OK with empty body)

#### `POST /api/v1/admin/figma/watched-files/{watchedFileId}/enable` — `[FIGMA-005]` 활성화

권한: `FIGMA.MANAGE`

비활성화된 파일을 다시 활성화. **Response 없음.**

#### `GET /api/v1/admin/figma/watched-files?enabled={true|false|미전달}` — `[FIGMA-008]` 목록

권한: `FIGMA.READ`

| 쿼리               | 동작             |
|------------------|----------------|
| 미전달              | 전체 (활성/비활성 모두) |
| `?enabled=true`  | 활성만            |
| `?enabled=false` | 비활성만           |

**Response**:

```json
[
    {
        "id": 17,
        "fileKey": "abcDEF12345",
        "displayName": "디자인 시스템 v3",
        "enabled": true,
        "lastSyncedAt": "2026-05-08T03:14:00Z",
        "lastError": null
    }
]
```

#### `GET /api/v1/admin/figma/watched-files/{watchedFileId}` — `[FIGMA-009]` 단건

권한: `FIGMA.READ`

**Response**: list 항목과 동일 객체. 미존재 시 `FIGMA-0006`.

### 2.3 Routing Domains — 분류 카테고리 + 담당자

#### `POST /api/v1/admin/figma/routing-domains` — `[FIGMA-011]` 도메인 등록

권한: `FIGMA.MANAGE`

**Request**:

```json
{
    "domainKey": "auth",
    "description": "인증/회원 관련 댓글",
    "discordWebhookUrl": "https://discord.com/api/webhooks/123456789012345678/abcdef...",
    "fallback": false
}
```

| 필드                  | 제약                    | 설명                                   |
|---------------------|-----------------------|--------------------------------------|
| `domainKey`         | 필수, 1~100, **UNIQUE** | LLM 분류 결과로 매칭될 키                     |
| `description`       | 선택, 0~500             | 운영자 주석                               |
| `discordWebhookUrl` | 필수                    | 매칭된 댓글이 발송될 Discord webhook          |
| `fallback`          | boolean               | true 면 fallback 도메인. 시스템 전체에 1 건만 권장 |

**Response**: `{ "domainId": 5 }`

**에러**: `FIGMA-0014` (동일 `domainKey` 존재)

#### `PATCH /api/v1/admin/figma/routing-domains/{domainId}` — `[FIGMA-019]` 도메인 수정

권한: `FIGMA.MANAGE`

`description`, `discordWebhookUrl`, `fallback` 을 수정한다. `domain_key` 는 LLM 분류 키이므로 변경 불가.

**Request**:

```json
{
    "description": "인증/회원 관련 댓글 (수정됨)",
    "discordWebhookUrl": "https://discord.com/api/webhooks/new-webhook-url",
    "fallback": false
}
```

| 필드                  | 제약       | 설명                                   |
|---------------------|----------|--------------------------------------|
| `description`       | 선택, 0~500 | 운영자 주석                               |
| `discordWebhookUrl` | 필수        | 새 Discord webhook URL                 |
| `fallback`          | boolean  | fallback 도메인 여부                       |

**Response**: 없음 (204)

**에러**: `FIGMA-0013` (도메인 미존재)

> `discordWebhookUrl` 은 항상 새 값 전체를 전송해야 한다. 백오피스 화면에서는 기존 URL 이 마스킹(`discordWebhookUrlMasked`) 되어 있으므로, 변경 시 운영자가 전체 URL 을 다시 입력하도록 UI 를 구성한다.

#### `DELETE /api/v1/admin/figma/routing-domains/{domainId}` — `[FIGMA-012]` 도메인 삭제

권한: `FIGMA.MANAGE`

**삭제 시 mention 도 cascade 로 함께 삭제된다.** 응답 본문 없음 (204).

#### `POST /api/v1/admin/figma/routing-domains/{domainId}/mentions` — `[FIGMA-013]` 멘션 추가

권한: `FIGMA.MANAGE`

**Request**:

```json
{
    "mentionId": "987654321987654321",
    "mentionType": "ROLE",
    "displayLabel": "백엔드 파트장"
}
```

| 필드             | 제약        | 설명                                  |
|----------------|-----------|-------------------------------------|
| `mentionId`    | 필수, 1~50  | Discord role 또는 user 의 snowflake ID |
| `mentionType`  | 필수        | `ROLE` 또는 `USER`                    |
| `displayLabel` | 선택, 0~255 | 백오피스 화면용 라벨                         |

**Response**: `{ "mentionId": 41 }`

#### `PATCH /api/v1/admin/figma/routing-domains/mentions/{mentionId}` — `[FIGMA-020]` 멘션 수정

권한: `FIGMA.MANAGE`

Discord ID(`mentionId`) 와 표시 라벨(`displayLabel`) 을 수정한다. `mentionType` (ROLE/USER) 변경은 삭제 후 재등록으로 처리한다.

**Request**:

```json
{
    "mentionId": "111222333444555666",
    "displayLabel": "백엔드 파트장 (업데이트됨)"
}
```

| 필드             | 제약        | 설명                                  |
|----------------|-----------|-------------------------------------|
| `mentionId`    | 필수, 1~50  | Discord role 또는 user 의 snowflake ID |
| `displayLabel` | 선택, 0~255 | 백오피스 화면용 라벨                         |

**Response**: 없음 (204)

**에러**: `FIGMA-0015` (mention 미존재)

#### `DELETE /api/v1/admin/figma/routing-domains/mentions/{mentionId}` — `[FIGMA-014]` 멘션 삭제

권한: `FIGMA.MANAGE`. 응답 본문 없음.

#### `GET /api/v1/admin/figma/routing-domains` — `[FIGMA-016]` 도메인 목록

권한: `FIGMA.READ`

**Response** — list 응답에서는 `mentions` 필드가 `null` (페이로드 통제):

```json
[
    {
        "id": 5,
        "domainKey": "auth",
        "description": "인증/회원 관련 댓글",
        "discordWebhookUrlMasked": "https://discord.com/****5678/****cdef",
        "fallback": false,
        "mentionCount": 2,
        "mentions": null
    }
]
```

> `discordWebhookUrlMasked` — ADR-005 마스킹 정책. 원본은 절대 노출되지 않으며, FE 는 마스킹 값을 그대로 화면에 출력한다. 사용자가 webhook 을 새로 등록하려면 처음부터 다시 입력해야 한다.

#### `GET /api/v1/admin/figma/routing-domains/{domainId}` — `[FIGMA-017]` 도메인 단건

권한: `FIGMA.READ`

list 와 동일하지만 `mentions` 가 채워진다.

```json
{
    "id": 5,
    "domainKey": "auth",
    "discordWebhookUrlMasked": "https://discord.com/****5678/****cdef",
    "fallback": false,
    "mentionCount": 2,
    "mentions": [
        {
            "id": 41,
            "domainId": 5,
            "mentionId": "987654321987654321",
            "mentionType": "ROLE",
            "displayLabel": "백엔드 파트장"
        }
    ]
}
```

미존재 시 `FIGMA-0013`.

#### `GET /api/v1/admin/figma/routing-domains/{domainId}/mentions` — `[FIGMA-018]` 멘션 목록만

권한: `FIGMA.READ`

부분 갱신 화면용 (예: 멘션만 다시 fetch). Response 는 위 `mentions[]` 와 동일 형태.

### 2.4 Sync / Digest / Preview — 시간창 발송

세 endpoint 모두 [FigmaSyncController](../../src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java) 한 곳에 있다. 부수효과의 강도가 다르므로 UI 에서 명확히 구분해야 한다.

| Endpoint                        | 발송 | dispatch 기록 | cursor 갱신 | 멱등            |
|---------------------------------|----|-------------|-----------|---------------|
| `POST /sync`                    | ✓  | ✓           | ✓         | ✓             |
| `POST /sync/watched-files/{id}` | ✓  | ✓           | ✓         | ✓             |
| `POST /digest?from=&to=`        | ✓  | ✗           | ✗         | ✗ (재호출 = 재발송) |
| `GET /preview?from=&to=`        | ✗  | ✗           | ✗         | (조회 전용)       |

#### `POST /api/v1/admin/figma/sync` — `[FIGMA-006]` 활성 파일 전체 즉시 sync

권한: `FIGMA.MANAGE`

Request 없음. Response 본문 없음 (204). 동기 처리이므로 응답이 돌아오면 발송 시도 완료 (실패 파일은 `lastError` 로 누적).

#### `POST /api/v1/admin/figma/sync/watched-files/{watchedFileId}` — `[FIGMA-007]` 단일 파일 즉시 sync

권한: `FIGMA.MANAGE`

`enabled` 여부와 무관하게 동작 (비활성 파일도 강제 sync 가능). Response 본문 없음.

#### `POST /api/v1/admin/figma/digest?from={Instant}&to={Instant}` — `[FIGMA-015]` catch-up 발송

권한: `FIGMA.MANAGE`

**Request 쿼리**:

| 쿼리     | 형식                                       | 설명                 |
|--------|------------------------------------------|--------------------|
| `from` | ISO8601 Instant (`2026-05-01T00:00:00Z`) | 시간창 시작 (inclusive) |
| `to`   | ISO8601 Instant                          | 시간창 끝 (inclusive)  |

**Response**:

```json
{
    "from": "2026-05-01T00:00:00Z",
    "to": "2026-05-08T00:00:00Z",
    "totalComments": 24,
    "unmatchedCount": 1,
    "domains": [
        {
            "domainKey": "auth",
            "commentCount": 12,
            "sent": true
        },
        {
            "domainKey": "fallback",
            "commentCount": 1,
            "sent": true
        }
    ]
}
```

**에러**: `FIGMA-0017` (시간창이 잘못된 경우 — 예: from > to)

#### `GET /api/v1/admin/figma/preview?from={Instant}&to={Instant}&watchedFileId={Long}` — `[FIGMA-010]` 미리보기

권한: `FIGMA.READ`

**Request 쿼리** — 모두 선택:

| 쿼리                  | 기본 동작                                             |
|---------------------|---------------------------------------------------|
| `from`, `to` 미전달    | `(now - 2 * pollInterval) ~ now` 시간창 사용 (기본 10 분) |
| `watchedFileId` 미전달 | 활성 파일 전체                                          |
| `watchedFileId` 전달  | 해당 파일 1 건 (enabled 무관)                            |

**Response** ([FigmaSummaryResult](../../src/main/java/com/umc/product/figma/application/port/in/dto/FigmaSummaryResult.java)):

```json
{
    "from": "2026-05-08T03:00:00Z",
    "to": "2026-05-08T03:10:00Z",
    "totalComments": 5,
    "unmatchedCount": 0,
    "skippedAlreadyDispatchedCount": 2,
    "domains": [
        {
            "domainKey": "auth",
            "webhookUrl": "https://discord.com/api/webhooks/...",
            "fallback": false,
            "mentionRenders": [
                "<@&987654321987654321>"
            ],
            "sent": false,
            "comments": [
                {
                    "commentId": "1234",
                    "message": "회원가입 화면에서 약관 체크가 안 풀려요",
                    "authorName": "디자이너 A",
                    "fileKey": "abcDEF12345",
                    "fileDisplayName": "디자인 시스템 v3",
                    "nodeId": "12:345",
                    "pageName": "Sign Up",
                    "classifiedDomainKey": "auth",
                    "createdAt": "2026-05-08T03:05:12Z",
                    "alreadyDispatched": false
                }
            ]
        }
    ]
}
```

> `alreadyDispatched=true` 인 댓글은 다음 정기 sync 에서 발송되지 않는다 (이미 보낸 댓글). UI 에서 회색으로 처리해 운영자가 "왜 안 보내지지?" 라고 헷갈리지 않도록 한다.
>
> `webhookUrl` 은 preview 응답에서는 마스킹되지 않은 원본이 내려온다. 화면에는 표시하지 않는 것을 권장 (FE 자체 마스킹 또는 숨김).

---

## 3. 에러 코드

| 코드           | HTTP | 의미                      | FE 처리 가이드                        |
|--------------|------|-------------------------|----------------------------------|
| `FIGMA-0001` | 404  | OAuth 통합 미등록            | "먼저 Figma OAuth 위임을 진행해주세요" 안내   |
| `FIGMA-0002` | 502  | 토큰 교환 실패                | OAuth 재시도 유도                     |
| `FIGMA-0003` | 502  | access token refresh 실패 | OAuth 재위임 유도                     |
| `FIGMA-0004` | 502  | Figma 댓글 조회 실패          | "Figma API 일시 장애" 토스트 + 재시도 버튼   |
| `FIGMA-0005` | 502  | Figma 파일 메타 실패          | 동일                               |
| `FIGMA-0006` | 404  | watched file 미존재        | "삭제되었거나 ID 가 잘못되었습니다"            |
| `FIGMA-0007` | 409  | 이미 등록된 fileKey          | 등록 폼에서 인라인 에러                    |
| `FIGMA-0008` | 400  | OAuth state 불일치         | OAuth 처음부터 재시작                   |
| `FIGMA-0009` | 500  | 토큰 암복호화 실패              | "관리자 문의" 안내                      |
| `FIGMA-0010` | 502  | Discord 발송 실패           | 토스트 + 자동 재시도 X (운영자 의도 확인 후 재발송) |
| `FIGMA-0013` | 404  | routing domain 미존재      | "삭제된 도메인" 안내                     |
| `FIGMA-0014` | 409  | domain_key 중복           | 등록 폼에서 인라인 에러                    |
| `FIGMA-0015` | 404  | mention 미존재             | 화면 새로고침                          |
| `FIGMA-0016` | 412  | 라우팅 도메인 0 건             | "먼저 라우팅 도메인을 등록해주세요" 강제 안내       |
| `FIGMA-0017` | 400  | digest from/to 시간창 오류   | from < to 입력 검증                  |

권한 거부 시에는 공통 코드 (`AUTHORIZATION-...`) 가 떨어지며, 이는 백오피스 진입 가드에서 차단되어야 한다.

---

## 4. 백오피스 화면 구성 instruction

### 4.1 정보 구조 (Information Architecture)

백오피스 사이드바에 **`Figma 통합`** 메뉴를 두고, 4 개 하위 페이지로 분리한다.

```
Figma 통합
├── 대시보드        (Flow B 의 시작점)
├── Watched Files
├── 라우팅 도메인
└── 운영 액션      (Flow C)
```

OAuth 위임은 **별도 페이지가 아닌 대시보드 우상단의 상태 카드** 로 노출한다. 이미 위임되어 있으면 "위임 완료" + owner 이름, 미위임이면 "Figma 위임 시작" CTA 버튼.

### 4.2 페이지별 구성

#### 페이지 1 — 대시보드

**목적**: Flow B 의 시작점. 한 화면에서 시스템 헬스를 본다.

**구성 (위→아래)**:

1. **OAuth 상태 카드** (1 줄)
    - 위임됨 / 미위임 binary 상태. 미위임이면 빨간 띠 + `[Figma 위임 시작]` 버튼.
    - 클릭 시 `GET /oauth` → 응답의 `authorizeUrl` 로 `window.location.href` redirect.
2. **헬스 카드 4 개** (가로 4 분할)
    - `라우팅 도메인 N 건 (fallback ✓/✗)` — `GET /routing-domains` 의 size + `fallback=true` 존재 여부
    - `Watched Files N 건 (활성 M / 비활성 K)` — `GET /watched-files` 카운트
    - `최근 24h 발송 ?` — preview 24h 시간창 호출해서 `totalComments - skippedAlreadyDispatchedCount` 표시
    - `에러 파일 N 건` — `GET /watched-files` 결과에서 `lastError != null` 카운트. 1 건 이상이면 빨간색.
3. **최근 sync 활동 테이블**
    - `GET /watched-files` 응답을 `lastSyncedAt desc` 로 정렬해 상위 5 건. 컬럼: `displayName / lastSyncedAt / lastError`.
4. **빠른 액션 패널**
    - `[전체 즉시 sync]` (POST `/sync`) — confirm 모달, 성공 토스트.
    - `[지금 미리보기]` (GET `/preview` 기본 시간창) — 결과는 사이드 drawer 로 표시.

#### 페이지 2 — Watched Files

**목적**: Flow A·D 의 파일 등록 / 비활성화.

**상단 툴바**:

- `[+ 신규 파일 등록]` 버튼 → 모달 (필드: `fileKey`, `displayName`)
- enabled 필터 (`전체` / `활성` / `비활성` 라디오 또는 segmented control) — `?enabled=` 쿼리 직접 매핑.

**테이블**:

- 컬럼: `displayName`, `fileKey` (모노스페이스 + `[복사]` 버튼), `enabled` (toggle), `lastSyncedAt` (상대 시간 + tooltip 절대), `lastError` (있으면 빨간색 짧은 표시 + hover 로 전문)
- row 액션 메뉴 (`⋯`):
    - `즉시 sync` → `POST /sync/watched-files/{id}`
    - `미리보기` → `GET /preview?watchedFileId={id}` → drawer
    - `enabled toggle` → 끄면 `DELETE /watched-files/{id}`, 켜면 `POST /watched-files/{id}/enable`. **즉시 호출 + 낙관적 업데이트**
    - `세부정보` → `/watched-files/{id}` 상세 페이지로 이동 (선택)

**비어 있는 상태**:

- "아직 등록된 파일이 없어요. Figma URL 의 `/file/{fileKey}/...` 에서 fileKey 를 복사해 등록하세요." + 신규 등록 CTA.

#### 페이지 3 — 라우팅 도메인

**목적**: Flow A·D 의 도메인 / 멘션 관리.

**상단 툴바**:

- `[+ 신규 도메인]` → 모달 (필드: `domainKey`, `description`, `discordWebhookUrl`, `fallback` toggle).
- `fallback` 도메인이 0 건이면 페이지 최상단에 빨간 banner: "fallback 도메인이 없습니다. 분류 실패 댓글이 발송되지 않습니다."

**좌우 2 분할 레이아웃**:

- **왼쪽 (1/3)** — 도메인 list (`GET /routing-domains`).
    - 각 row: `domainKey` (큰 글자) + `description` (작은 글자) + `mentionCount` 뱃지 + `fallback` 일 때 ⭐ 표시.
    - 선택된 row 는 강조.
- **오른쪽 (2/3)** — 선택된 도메인 상세 (`GET /routing-domains/{id}`).
    - **도메인 수정 폼**: `description`, `discordWebhookUrl` (항상 빈 칸으로 제공 — 기존 값은 마스킹되어 알 수 없음), `fallback` toggle. `[저장]` 버튼 → `PATCH /routing-domains/{id}`. 성공 시 좌측 list 를 re-fetch.
    - `discordWebhookUrlMasked` 는 현재 설정 확인용으로 폼 위에 별도 표시. 수정 시 운영자가 새 URL 전체를 입력해야 함을 tooltip 으로 안내.
    - **멘션 섹션**:
        - 각 멘션 row: `displayLabel` (없으면 `mentionId`) + `[ROLE/USER]` 뱃지 + `[수정]` 버튼 (인라인 편집 모드 진입 → `mentionId` · `displayLabel` 수정 가능 → `PATCH /routing-domains/mentions/{id}`) + `[삭제]` 버튼 (`DELETE /routing-domains/mentions/{id}`).
        - `mentionType` 변경은 삭제 후 재등록만 가능. 수정 UI 에서 `mentionType` 필드는 비활성화하고 "(변경 불가 — 삭제 후 재등록)" 으로 표시.
        - 하단에 inline 추가 폼: `mentionId`, `mentionType` 셀렉트, `displayLabel` (선택). `[추가]` 버튼 → `POST /routing-domains/{id}/mentions`.
    - 페이지 우상단에 `[도메인 삭제]` 버튼 — confirm 모달 ("멘션도 함께 삭제됩니다") + `DELETE /routing-domains/{id}`.

**Discord ID 도움말**: 운영자가 role/user ID 를 모를 수 있으므로 멘션 추가 폼 아래에 helper 링크: "Discord 에서 ID 확인하는 법 →" (외부 문서 링크).

#### 페이지 4 — 운영 액션

**목적**: Flow C 의 ad-hoc 액션 모음. **위험도 순으로 위→아래** 배치.

**섹션 1 — Preview (안전, 부수효과 없음)**

- 폼: `from` / `to` (datetime-local, 기본값 비워두면 백엔드가 자동 채움) + `watchedFileId` 셀렉트 (선택).
- `[미리보기]` 버튼 → `GET /preview?...`.
- 결과는 페이지 하단에 inline 표시. 도메인별 묶음, 댓글 카드 (`alreadyDispatched=true` 회색 처리, `skippedAlreadyDispatchedCount` 별도 강조).

**섹션 2 — 즉시 Sync (멱등, 안전)**

- `[활성 파일 전체 sync]` 버튼 → `POST /sync`.
- 단일 파일 sync 는 Watched Files 페이지에서 row 액션으로 노출 (이 페이지에 중복 노출 X).
- confirm 모달은 가벼운 수준 ("실행하시겠어요?" + 확인).

**섹션 3 — Digest catch-up (비-멱등, 위험)**

- 배경색을 빨간 톤으로 깔고 상단에 경고 박스: **"이 액션은 동일 시간창을 두 번 호출하면 댓글이 두 번 발송됩니다."**
- 폼: `from` / `to` 필수 입력 (둘 다 비어 있으면 비활성화).
- `[Digest 발송]` 버튼 → 강한 confirm 모달:
    - 시간창 표시 + "이 시간창의 댓글이 도메인별로 다시 Discord 로 발송됩니다."
    - 운영자가 textarea 에 `digest` 라고 직접 타이핑해야 활성화되는 confirm 패턴 권장.
- 응답의 `domains[].sent=false` 는 "발송 실패" 로 빨간색, true 는 "발송됨" 으로 녹색 처리.

### 4.3 공통 컴포넌트 / UX 정책

- **권한 가드**: 라우터 진입 시 현재 사용자의 role 이 `SUPER_ADMIN` 이 아니면 메뉴 자체를 숨기거나 403 페이지로 보낸다 (서버 응답을 기다리지 말고 클라이언트에서 1 차 차단).
- **시간 표시**: 모든 `Instant` 필드는 사용자 로컬 타임존으로 표시하되, hover tooltip 에 ISO8601 절대값을 보여준다.
- **webhook URL 표기**: 마스킹된 형태(`discordWebhookUrlMasked`) 만 화면에 노출. 절대 평문 webhook 을 보여주지 않는다 (preview 응답의 `webhookUrl` 도 화면에 출력 X).
- **에러 토스트**: `ApiResponse.code` 값을 기반으로 메시지를 매핑 (위 §3 표). FIGMA-0010 같은 외부 의존 실패는 자동 재시도 X.
- **장시간 호출 로딩 UI**: `POST /sync`, `POST /digest` 는 활성 파일 수에 따라 수 초 ~ 수십 초 걸릴 수 있다. 버튼 클릭 → 로딩 상태 (spinner + 비활성화) → 응답 후 토스트.
- **공통 가드 — fallback 도메인 부재 시 알림**: Flow B 진입 시 백그라운드로 `GET /routing-domains` 를 한 번 호출해 `fallback=true` 가 0 건이면 글로벌 banner 로 경고. UMC 운영진이 셋업을 잊지 않도록.

### 4.4 인터랙션 시퀀스 다이어그램 (text)

#### Flow A — 셋업

```
[FE]                        [BE]
  │                           │
  │─ GET /oauth ─────────────▶│
  │◀─ { authorizeUrl, state }─│
  │  window.location = url    │
  │                           │
  │ Figma 동의 화면            │
  │  redirect→callback        │
  │                           │
  │─ POST /routing-domains ──▶│  (×N 도메인)
  │◀─ { domainId } ───────────│
  │─ POST /…/mentions ───────▶│  (×M 멘션)
  │◀─ { mentionId } ──────────│
  │─ POST /watched-files ────▶│  (×K 파일)
  │◀─ { watchedFileId } ──────│
  │─ GET /preview?wfId=… ────▶│  (검증)
  │◀─ FigmaSummaryResult ─────│
  │─ POST /sync ─────────────▶│  (1회 비우기)
  │◀─ 204 ────────────────────│
```

#### Flow C — Digest catch-up

```
[FE 운영자]                   [BE]
  │ from/to 입력               │
  │ [Digest] 클릭             │
  │ confirm 모달 → "digest"   │
  │  타이핑 후 확인            │
  │─ POST /digest?from&to ──▶│
  │  (수 초~수십 초 대기)     │
  │◀─ FigmaDigestSummary ─────│
  │ 도메인별 sent 결과 표시   │
```

---

## 5. 참고

- ADR
    - [ADR-003 Figma 댓글 Discord 포워딩](../adr/003-figma-comment-discord-forwarder.md)
    - [ADR-004 시간창 기반 단일 유즈케이스 통합](../adr/004-figma-comment-time-window-unification.md)
    - [ADR-005 Routing/Watched-File Query API](../adr/005-figma-routing-and-watched-file-query-apis.md)
    - [ADR-007 Figma admin API SUPER_ADMIN 전용](../adr/007-figma-admin-api-super-admin-only.md)
- 관련 컨트롤러
    - [FigmaOAuthController](../../src/main/java/com/umc/product/figma/adapter/in/web/FigmaOAuthController.java)
    - [FigmaWatchedFileController](../../src/main/java/com/umc/product/figma/adapter/in/web/FigmaWatchedFileController.java)
    - [FigmaRoutingDomainController](../../src/main/java/com/umc/product/figma/adapter/in/web/FigmaRoutingDomainController.java)
    - [FigmaSyncController](../../src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java)
- Swagger UI: `${API_HOST}/docs` (활성화 환경에서)
