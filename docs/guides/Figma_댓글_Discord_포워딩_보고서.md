# Figma 댓글 Discord 포워딩 시스템 설계/구현 보고서

## 1. 개요

본 문서는 [ADR-003: Figma 파일 댓글을 OAuth 기반으로 폴링해 담당 파트별 Discord 멘션으로 전달](../adr/003-figma-comment-discord-forwarder.md) 결정에 따라 신설된 `figma` 도메인의 설계 의도, 아키텍처, 운영 흐름, API, 데이터 모델, 핵심 용어를 정리한다.
실제 구현은 `com.umc.product.figma` 패키지에 위치하며, 본 문서는 운영진이 시스템을 사용/관리/디버깅할 수 있도록 도메인 지식 + 사용 절차를 한 곳에 모은 운영 가이드 성격을 갖는다.

## 2. 육하원칙(6W1H) 정리

| 구분             | 내용                                                                                                                                                                                                                                  |
|----------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **누가(Who)**    | UMC PRODUCT 운영진 1인 (Figma OAuth 동의자) 와 백엔드 시스템                                                                                                                                                                                       |
| **언제(When)**   | 운영진이 등록한 Figma 파일에 댓글이 달릴 때마다 (스케줄러 5분 주기 + 운영진 on-demand 트리거 시점)                                                                                                                                                                    |
| **어디서(Where)** | `com.umc.product.figma` 도메인. 외부 의존성: Figma REST API (댓글 / 파일 메타데이터 / OAuth), Discord Webhook                                                                                                                                        |
| **무엇을(What)**  | Figma 파일 댓글을 폴링해 LLM 으로 담당 도메인을 분류하고, 도메인별 담당자 mention 과 함께 Discord embed 메시지를 발송. preview / 수동 트리거 / 라우팅/멘션 관리 admin API 포함                                                                                                |
| **왜(Why)**     | (1) Figma 기본 알림이 watcher/멘션된 사람에게만 도달해 파트장 누락, (2) 디자인 피드백이 Discord 와 분리되어 두 채널 병행 확인 필요, (3) 댓글이 어느 도메인 책임인지 매번 사람이 판단해야 하는 인지 부담                                                                                          |
| **어떻게(How)**   | (1) OAuth Authorization Code Flow 로 운영진 위임 토큰 확보, (2) `@Scheduled` 폴링으로 신규 댓글만 필터, (3) `ChatCompleteUseCase` 로 댓글 → 등록된 `domain_key` 분류, (4) 매칭된 `figma_routing_domain` 의 webhook + N개 mention 으로 Discord embed 송신 (실패 시 fallback), (5) `last_synced_comment_id` 갱신 |

## 3. 핵심 용어

### 3.1 Figma 파일 구조와 "노드(node)"

Figma 파일은 다음과 같은 트리 구조를 갖는다.

```
File (file_key 로 식별)
├── Page (= CANVAS, 디자이너가 작업하는 캔버스 단위)
│   ├── Frame (= 화면/레이아웃 컨테이너, 예: "로그인 화면")
│   │   ├── Group / Component / Layer ...
│   │   └── ...
│   └── Frame
└── Page
```

이 트리에 등장하는 **모든 박스/레이어/페이지/프레임/컴포넌트/그룹 등은 모두 "노드(node)" 라는 동일 추상으로 다뤄지며**, 각 노드는 `node_id`(예: `"10:42"`) 라는 고유 식별자를 갖는다.
즉 "노드" 는 Figma 의 물리적 단위가 아니라 **트리 위 한 점** 을 가리키는 일반 용어다.

댓글 입장에서 "어떤 노드에 달렸는지" 가 중요한 이유는 다음과 같다.

- 댓글이 빈 캔버스에 달리면 → `node_id` 없음 (`null`)
- 댓글이 특정 frame/layer 에 달리면 → 그 노드의 `node_id` 가 응답에 포함됨 (`client_meta.node_id`)
- 본 시스템은 **그 노드의 가장 가까운 상위 페이지(CANVAS) 이름** 을 라우팅 키로 사용한다.

예시:
```
Page "디자인" (CANVAS)
└── Frame "로그인 화면" (id="10:1")
    └── Layer "버튼" (id="10:42")  ← 여기에 댓글이 달렸다면
```
→ 라우팅에 사용되는 페이지 이름은 `"디자인"` 이며,
운영진은 `figma_part_route` 에 `(file_key, page_name="디자인", part_key="DESIGN", role_id=..., webhook=...)` 행을 등록해 둔다.

### 3.2 파일 키(file_key)

Figma URL 의 일부로, 파일을 식별하는 영문/숫자 키.
예: `https://www.figma.com/file/`**`abc123XYZ`**`/landing-v2` → `file_key = abc123XYZ`.

### 3.3 댓글 ID(comment_id)

Figma 댓글 한 건의 식별자. 본 시스템은 파일별 마지막 처리한 `comment_id` 를 보관해 다음 폴링에서 그보다 새로운 댓글만 발송한다 (중복 방지).

### 3.4 위임자(delegator) / 통합(integration)

운영진 1인이 Figma OAuth 동의를 거쳐 발급받은 refresh token + access token 묶음을 "통합(integration)" 이라 부른다. 서버는 access token 만료 시 자동으로 refresh 한다.

### 3.5 라우팅 도메인(routing domain) / 멘션(mention) / fallback

`figma_routing_domain` 한 행 = `(domain_key, description, discord_webhook_url, fallback)` 의 조합. **댓글이 LLM 으로 어떤 `domain_key` 로 분류되었는가** 가 곧 어떤 채널/멘션으로 보낼지를 결정한다.
한 도메인은 1:N 으로 `figma_routing_domain_mention` (`mention_id`, `mention_type` ∈ {ROLE, USER}, `display_label`) 을 가지며, 메시지의 `content` 에 `<@&id>` / `<@id>` 로 렌더되어 Discord 알림을 발생시킨다.
`fallback=true` 인 도메인 1건은 LLM 분류가 후보 외 값을 반환하거나 호출이 실패한 댓글의 안전망이다.

### 3.6 LLM 도메인 / provider

`com.umc.product.llm` 으로 분리된 별도 도메인. `ChatCompleteUseCase` 가 진입점이며 `ChatCompletionPort` 구현체를 `app.llm.provider` 로 교체한다.
본 시점은 `MockChatCompletionAdapter` (provider=mock) 만 활성화되어 후보 중 하나를 무작위 반환한다. 실제 OpenAI / Gemini / Spring AI 어댑터는 후속 PR 에서 동일 인터페이스로 추가된다.

## 4. 아키텍처

본 시스템은 두 도메인으로 구성된다: 외부 시스템 통합 도메인 `figma`, LLM 호출 추상 도메인 `llm`. 둘 다 헥사고날 구조.

```
com.umc.product.figma
├── domain/
│   ├── FigmaIntegration                 # OAuth 토큰 보관 + 만료 판정 + 회전
│   ├── FigmaWatchedFile                 # 폴링 대상 파일 + sync 상태
│   ├── FigmaRoutingDomain               # domain_key → webhook + fallback
│   ├── FigmaRoutingDomainMention        # 도메인의 mention 대상 1건 (ROLE/USER)
│   ├── enums/DiscordMentionType         # ROLE/USER 렌더링 책임
│   └── exception/FigmaErrorCode, FigmaDomainException
│
├── application/
│   ├── port/in/
│   │   ├── RegisterFigmaIntegrationUseCase    # OAuth 등록 + state 발급/소비
│   │   ├── ManageFigmaWatchedFileUseCase      # 파일 등록/활성화/비활성화
│   │   ├── ManageFigmaRoutingDomainUseCase    # 도메인 + mention CRUD
│   │   ├── SyncFigmaCommentsUseCase           # 동기화 (전체 / 단일 파일)
│   │   └── PreviewFigmaCommentsUseCase        # 발송 없이 read-only 미리보기
│   ├── port/out/
│   │   ├── Load/SaveFigmaIntegrationPort
│   │   ├── Load/SaveFigmaWatchedFilePort
│   │   ├── Load/SaveFigmaRoutingDomainPort
│   │   ├── FigmaOAuthPort               # token 교환 / refresh
│   │   ├── FetchFigmaCommentPort        # 댓글 조회
│   │   ├── FetchFigmaFileMetadataPort   # node_id → 페이지명 (embed 부가 정보용)
│   │   └── SendDiscordMentionPort       # 도메인별 webhook embed 멘션 발송
│   └── service/
│       ├── FigmaOAuthStateStore         # state ↔ memberId 바인딩 (in-memory)
│       ├── FigmaTokenCipher             # token AES-GCM 암복호화
│       ├── FigmaIntegrationCommandService
│       ├── FigmaWatchedFileCommandService
│       ├── FigmaRoutingDomainCommandService
│       ├── FigmaCommentDomainClassifier # ChatCompleteUseCase 호출 → domain_key
│       ├── FigmaCommentSyncCommandService     # 오케스트레이션 (파일 루프)
│       ├── FigmaSingleFileSyncProcessor       # 파일 단건 REQUIRES_NEW
│       └── FigmaCommentPreviewQueryService
│
└── adapter/
    ├── in/
    │   ├── web/      # admin REST 컨트롤러 5종
    │   └── scheduler/FigmaCommentSyncScheduler
    └── out/
        ├── persistence/  # JPA 레포지토리 + PersistenceAdapter
        └── external/     # Figma REST 클라이언트 + Discord 멘션 embed 어댑터

com.umc.product.llm
├── domain/exception/LlmErrorCode, LlmDomainException
├── application/
│   ├── port/in/  ChatCompleteUseCase (+ ChatCompleteCommand, ChatCompletionResult)
│   ├── port/out/ ChatCompletionPort (provider 추상)
│   └── service/  ChatCompletionService (단일 활성 어댑터에 위임)
└── adapter/out/external/
    └── MockChatCompletionAdapter        # provider=mock, 후보 중 무작위 반환
```

핵심 의존 방향: `adapter/in → application/service → application/port → domain`.
`figma` 도메인은 `llm.application.port.in.ChatCompleteUseCase` 만 의존해 다른 LLM 어댑터로의 교체에 영향받지 않는다.

## 5. 데이터 모델

Flyway 마이그레이션:
- `V2026.05.07.10.00__create_figma_tables.sql` — figma_integration / figma_watched_file (figma_part_route 는 같은 마이그레이션에서 생성되었으나 10.20 에서 DROP)
- `V2026.05.07.10.10__create_figma_routing_domain_tables.sql` — figma_routing_domain / figma_routing_domain_mention 신규
- `V2026.05.07.10.20__drop_figma_part_route.sql` — page_name 기반 매핑 테이블 폐기

| 테이블                              | 컬럼                                                                                                              | 인덱스/제약                                                          |
|----------------------------------|-----------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------|
| `figma_integration`              | id, owner_member_id, refresh_token_enc, access_token_enc, access_token_expires_at, scope, created_at, updated_at | UNIQUE(`owner_member_id`)                                      |
| `figma_watched_file`             | id, file_key, display_name, enabled, last_synced_comment_id, last_synced_at, last_error, created_at, updated_at  | UNIQUE(`file_key`), INDEX(`enabled`)                           |
| `figma_routing_domain`           | id, domain_key, description, discord_webhook_url, fallback, created_at, updated_at                              | UNIQUE(`domain_key`), INDEX(`fallback`)                        |
| `figma_routing_domain_mention`   | id, domain_id (FK CASCADE), mention_id, mention_type ENUM(ROLE/USER), display_label, created_at, updated_at      | INDEX(`domain_id`), UNIQUE(`domain_id`, `mention_type`, `mention_id`) |

## 6. 라우팅 상세 (LLM 기반)

핵심 라우팅 단위는 `figma_routing_domain` 의 한 행이다 — **댓글이 LLM 으로 어떤 `domain_key` 로 분류되었는가** 가 곧 어디로 보낼지를 결정한다.

### 6.1 라우팅 도메인 / 멘션 행의 구성 요소

`figma_routing_domain`

| 컬럼                    | 타입           | 의미                                                                                            |
|-----------------------|--------------|-----------------------------------------------------------------------------------------------|
| `domain_key`          | VARCHAR(100) | LLM 분류 결과로 매칭될 키. 예: `"AUTH"`, `"SCHEDULE"`, `"NOTIFICATION"`. UNIQUE.                         |
| `description`         | VARCHAR(500) | LLM 에게 도메인의 의미를 알리는 보조 설명 (선택, 후속 PR 에서 prompt 에 활용 예정).                                       |
| `discord_webhook_url` | TEXT         | 메시지를 보낼 Discord 채널의 webhook URL.                                                              |
| `fallback`            | BOOLEAN      | `true` 면 LLM 분류 실패 / 후보 외 응답 시 도달할 기본 도메인. 전 시스템에 1건 권장.                                       |

`figma_routing_domain_mention` (도메인 1건당 N개)

| 컬럼              | 타입           | 의미                                                                                |
|-----------------|--------------|-----------------------------------------------------------------------------------|
| `domain_id`     | BIGINT FK    | `figma_routing_domain.id` — 도메인 삭제 시 cascade.                                      |
| `mention_id`    | VARCHAR(50)  | Discord snowflake ID (role 또는 user 의 ID).                                         |
| `mention_type`  | ENUM         | `ROLE` 이면 `<@&{id}>` 로 렌더, `USER` 면 `<@{id}>` 로 렌더.                                |
| `display_label` | VARCHAR(255) | 운영진 식별용 메모 (예: "디자인 파트장 김OO").                                                     |

UNIQUE 제약:
- `figma_routing_domain.domain_key`
- `figma_routing_domain_mention(domain_id, mention_type, mention_id)` — 같은 도메인에 같은 mention 이 중복 등록되지 않도록 보장

### 6.2 매칭 알고리즘

```
입력: comment {commentId, message, authorName, nodeId, createdAt}, watchedFile {fileKey, ...}

1. domains = SELECT * FROM figma_routing_domain          // 도메인이 0건이면 sync skip + 에러 기록
   candidates = domains.map(d -> d.domain_key)

2. (best-effort, 라우팅에는 영향 없음)
   nodeId 가 있으면 Figma REST 로 페이지명 해석 → embed 의 Page 필드용

3. classified = FigmaCommentDomainClassifier.classify(comment, candidates)
   = ChatCompleteUseCase.complete(systemPrompt, userPrompt(comment), candidates)
   → 응답이 candidates 에 포함되지 않거나 호출 실패면 null

4. matched = classified != null ? domains[classified] : null

5. applied = matched != null ? matched
            : (domains 중 fallback=true 인 첫 도메인)
            : (없으면) unmatched → 발송 skip + WARN 로그

6. mentions = SELECT * FROM figma_routing_domain_mention WHERE domain_id = applied.id
   mentionRenders = mentions.map(m -> m.render())   // ["<@&123>", "<@456>"]

7. Discord embed payload 빌드 (commit 6 의 포맷) → webhook 전송

8. last_synced_comment_id 갱신 (unmatched/매칭 무관)
```

핵심 포인트:
- **페이지명은 라우팅 키가 아니다** — embed 의 부가 정보일 뿐. 페이지가 변경되어도 라우팅에 영향 없음.
- LLM 응답이 후보 외 값이면 자동으로 fallback 으로 빠진다. **fallback 도메인 1건은 사실상 필수**.
- `last_synced_comment_id` 는 항상 갱신되어 동일 댓글이 다음 폴링에서 재처리되지 않는다 (중복 발송 방지 우선).

### 6.3 LLM 분류 호출 형태

`FigmaCommentDomainClassifier.classify` 가 만드는 ChatCompleteCommand:

```
systemPrompt: "너는 Figma 디자인 파일에 달린 댓글을 읽고 그것이 서버 프로젝트의 어느 도메인과
              가장 관련 있는지 분류하는 라우터다. 반드시 후보 도메인 키 중 정확히 하나만,
              다른 설명 없이 그 키 문자열만 반환하라."
userPrompt:   "[후보 도메인 키]\n{candidates join ', '}\n\n[댓글 작성자]\n{authorName}\n\n[댓글 본문]\n{message}"
candidates:   [도메인 키 리스트]
```

provider 가 `mock` 이면 `MockChatCompletionAdapter` 가 후보 중 하나를 무작위 반환하므로 분류 정확도는 의미가 없고, 흐름 검증/통합 테스트 용도로만 동작한다. 실제 `openai` / `gemini` / `spring-ai` provider 어댑터는 동일 인터페이스로 후속 PR 에서 추가된다.

### 6.4 등록 예시

운영진이 새 파일을 추가하고 라우팅을 구성하는 권장 절차:

```bash
# 1) (도메인이 처음이라면) 라우팅 도메인 등록
POST /api/v1/admin/figma/routing-domains
{
  "domainKey": "AUTH",
  "description": "회원 인증/로그인/JWT 관련",
  "discordWebhookUrl": "https://discord.com/api/webhooks/AAA/aaa",
  "fallback": false
}

POST /api/v1/admin/figma/routing-domains
{
  "domainKey": "SCHEDULE",
  "description": "일정/출석/스터디그룹 일정",
  "discordWebhookUrl": "https://discord.com/api/webhooks/BBB/bbb",
  "fallback": false
}

# 2) 도메인별 담당자 mention 추가 (N명 등록 가능)
POST /api/v1/admin/figma/routing-domains/{authDomainId}/mentions
{ "mentionId": "1234567890", "mentionType": "ROLE", "displayLabel": "AUTH 담당 role" }

POST /api/v1/admin/figma/routing-domains/{authDomainId}/mentions
{ "mentionId": "9876543210", "mentionType": "USER", "displayLabel": "김OO" }

# 3) fallback 도메인 (안전망)
POST /api/v1/admin/figma/routing-domains
{
  "domainKey": "FALLBACK",
  "description": "분류 실패 댓글 도착지",
  "discordWebhookUrl": "https://discord.com/api/webhooks/ZZZ/zzz",
  "fallback": true
}

# 4) Figma 파일 등록
POST /api/v1/admin/figma/watched-files
{ "fileKey": "abc123XYZ", "displayName": "랜딩 페이지 V2" }

# 5) preview 로 분류 결과 확인
GET /api/v1/admin/figma/sync/watched-files/{id}/preview
```

### 6.5 운영 시 주의사항

| 상황                              | 영향                                          | 대응                                                                       |
|---------------------------------|---------------------------------------------|--------------------------------------------------------------------------|
| 도메인이 한 건도 등록되지 않음              | sync 가 ROUTING_DOMAIN_NOT_REGISTERED 로 skip  | 최소 1건 등록. fallback 1건도 권장.                                              |
| fallback 미등록 + LLM 분류 실패         | unmatched, 발송 skip                          | fallback=true 행 1건 등록.                                                   |
| LLM 비용/지연                       | preview / sync 마다 호출                        | provider=mock 으로 흐름만 검증, 실 호출은 후속 PR 에서 도입 시 cache/배치 옵션 검토.            |
| 같은 도메인에 같은 mention 중복 추가         | UNIQUE(domain_id, mention_type, mention_id) 충돌 | 기존 mention DELETE 후 재등록.                                                 |
| 운영진이 도메인 키를 너무 많이 등록            | LLM prompt 가 길어짐, 분류 정확도 저하                  | 도메인 키는 큰 책임 단위로 (예: 10개 이하) 유지. description 으로 의미 보조.                    |
| 모델이 근거 없이 한 도메인만 반환             | 분류 정확도 의심                                   | preview 로 다수 댓글 결과 점검, system prompt 보강 또는 provider 교체 검토.                |

### 6.6 `domain_key` 명명 규칙

`domain_key` 는 LLM 이 응답 후보로 매칭할 키이자 embed Domain 필드에 노출되는 라벨이다. 권장:
- 서버 도메인 패키지명과 일치시키면 운영진 직관 부합 (`AUTH`, `SCHEDULE`, `NOTIFICATION`, `CHALLENGER`, `PROJECT` 등).
- 너무 세부적으로 쪼개면 (`SCHEDULE_ATTENDANCE`, `SCHEDULE_GROUP_CALENDAR` 등) LLM 분류 모호도가 높아져 정확도가 떨어진다.

### 6.7 분류·라우팅 결과 매트릭스

| LLM 응답           | 후보 포함 여부 | fallback 도메인 | 결과                                |
|------------------|----------|-------------|-----------------------------------|
| `"AUTH"`         | ✓        | (무관)        | AUTH 도메인의 webhook + mentions 발송   |
| `"UNKNOWN"`      | ✗        | 있음          | fallback 도메인으로 발송                 |
| `"UNKNOWN"`      | ✗        | 없음          | unmatched, 발송 skip + WARN 로그       |
| 호출 실패 / 예외      | -        | 있음          | fallback 도메인으로 발송                 |
| 호출 실패 / 예외      | -        | 없음          | unmatched, 발송 skip + WARN 로그       |
| 도메인 0건 등록        | -        | -           | sync 자체가 ROUTING_DOMAIN_NOT_REGISTERED 로 skip |

`unmatched` 든 매칭이든 `last_synced_comment_id` 는 갱신된다 → 같은 댓글이 다음 폴링에서 재처리되지 않으므로, fallback 채널 점검과 preview 사전 검증이 분류 정확도 모니터링 핵심.

## 7. 운영 흐름

### 7.1 최초 셋업 (운영진이 1회 수행)

1. 백오피스 로그인 (UMC PRODUCT JWT 보유 상태).
2. `GET /api/v1/admin/figma/oauth` 호출 → 응답의 `authorizeUrl` 로 브라우저 redirect.
3. Figma 동의 화면에서 "허용" 클릭.
4. Figma 가 우리 서버의 `/callback?code=...&state=...` 으로 redirect → 서버가 code → token 교환 후 `figma_integration` 에 저장.
5. `POST /api/v1/admin/figma/routing-domains` 로 LLM 분류 결과로 매칭될 도메인을 등록 (예: AUTH, SCHEDULE, NOTIFICATION).
6. 각 도메인에 `POST /api/v1/admin/figma/routing-domains/{id}/mentions` 로 담당자 (Discord role 또는 user) 를 N명까지 추가.
7. 분류 실패 안전망으로 `fallback=true` 도메인 1건도 등록 권장.
8. `POST /api/v1/admin/figma/watched-files` 로 폴링할 Figma 파일 등록.
9. `GET /api/v1/admin/figma/sync/watched-files/{id}/preview` 로 분류 결과를 사전 검증.

### 7.2 정상 동작 (자동)

```
[Scheduler 5분 주기]
        │
        ▼
SyncFigmaCommentsUseCase.syncAll()
        │
        ▼
활성 watched file 목록 조회 → access token 확보(필요 시 refresh)
        │
        ▼  (파일별 REQUIRES_NEW 트랜잭션)
FigmaSingleFileSyncProcessor.process(fileId, accessToken)
        │
        ├─ Figma REST: GET /v1/files/:file_key/comments
        ├─ last_synced_comment_id 이후 댓글만 필터
        ├─ Figma REST: GET /v1/files/:file_key/nodes?ids=... → CANVAS ancestor 이름 해석
        ├─ pageName 으로 figma_part_route 매핑 (없으면 fallback)
        ├─ Discord webhook 송신 (allowed_mentions.parse=["roles"])
        └─ last_synced_comment_id, last_synced_at 갱신
```

### 7.3 운영진 on-demand 액션

| 상황                         | 호출                                                                       |
|----------------------------|--------------------------------------------------------------------------|
| "지금 바로 sync 하고 싶다"          | `POST /api/v1/admin/figma/sync`                                          |
| "이 파일만 즉시 sync"             | `POST /api/v1/admin/figma/sync/watched-files/{id}`                       |
| "Discord 발송 없이 무엇이 갈지 보고 싶다" | `GET  /api/v1/admin/figma/sync/watched-files/{id}/preview`               |
| 라우팅 매핑 점검/추가/삭제             | `GET/POST/DELETE /api/v1/admin/figma/part-routes` 계열                     |
| 파일 비활성화/재활성화                | `DELETE/POST /api/v1/admin/figma/watched-files/{id}[/enable]`            |
| 동의자 교체                     | 새 운영진이 5.1 흐름을 다시 수행. 같은 owner_member_id 면 토큰 덮어씀, 다른 사람이면 새 통합 행이 생성됨. |

## 8. 핵심 보안 결정

### 8.1 OAuth state 에 memberId 바인딩

callback 은 Figma 에서 우리 서버로 들어오는 단순 브라우저 redirect 이므로 JWT 가 실리지 않는다. 따라서 callback 에서 위임자 memberId 를 인증 컨텍스트로 알 수 없다.
이 문제를 해결하기 위해 `FigmaOAuthStateStore` 가 다음을 보장한다.

- state = 256-bit `SecureRandom` URL-safe 문자열 (추측 불가)
- 발급 시 `(memberId, expiresAt)` 을 함께 보관 (`/start` 는 인증 필수이므로 자기 ID 만 묶을 수 있음)
- 10분 TTL + 신규 발급 시 lazy cleanup
- `consume()` 가 `ConcurrentHashMap.remove()` 로 원자적 lookup + 제거 → replay 차단

한계: in-memory store. 다중 인스턴스 환경에서는 sticky session / Redis 로 교체 필요. ADR 의 단일 위임자/저빈도 호출 전제 하에서는 충분.

### 8.2 토큰 암호화

`FigmaTokenCipher` 가 application property 기반 키를 SHA-256 으로 정규화 후 AES-GCM 으로 암복호화한다. refresh / access token 둘 다 평문으로 DB 저장하지 않는다. 운영 정책 결정 후 KMS / Jasypt 로 보강 가능하도록 인터페이스를 단순화해 두었다.

### 8.3 중복 발송 방지

발송 실패 시 트랜잭션 롤백 대신 `last_error` 만 기록한다. 즉, 한 번 Discord 로 나간 댓글은 다음 폴링에서 다시 보내지 않는다 (중복 발송 방지 우선). Discord 송신 실패는 운영진이 `last_error` 를 보고 사후 대응한다.

### 8.4 read-only 트랜잭션에서 token refresh

preview 는 `@Transactional(readOnly=true)` 인데, 그 안에서 `resolveActiveAccessToken()` 호출 시 토큰 만료라면 DB 쓰기가 필요하다. 충돌을 막기 위해 해당 메서드는 `@Transactional(propagation=REQUIRES_NEW)` 로 별도 쓰기 트랜잭션을 갖는다.

## 9. API 목록

| 메서드   | 경로                                                                       | 인증        | 용도                                              |
|-------|--------------------------------------------------------------------------|-----------|-------------------------------------------------|
| GET   | `/api/v1/admin/figma/oauth`                                              | 인증 필요     | Figma OAuth authorize URL 발급                    |
| GET   | `/api/v1/admin/figma/oauth/callback`                                     | `@Public` | Figma 동의 후 redirect 받는 콜백                       |
| POST  | `/api/v1/admin/figma/watched-files`                                      | 인증 필요     | 폴링 대상 파일 등록                                     |
| POST  | `/api/v1/admin/figma/watched-files/{id}/enable`                          | 인증 필요     | 파일 재활성화                                         |
| DELETE| `/api/v1/admin/figma/watched-files/{id}`                                 | 인증 필요     | 파일 비활성화                                         |
| POST  | `/api/v1/admin/figma/routing-domains`                                    | 인증 필요     | 라우팅 도메인 등록 (domain_key UNIQUE)                  |
| DELETE| `/api/v1/admin/figma/routing-domains/{id}`                               | 인증 필요     | 라우팅 도메인 삭제 (mention cascade)                    |
| POST  | `/api/v1/admin/figma/routing-domains/{id}/mentions`                      | 인증 필요     | 도메인에 담당자 mention (ROLE/USER) 추가                 |
| DELETE| `/api/v1/admin/figma/routing-domains/mentions/{id}`                      | 인증 필요     | 담당자 mention 삭제                                  |
| POST  | `/api/v1/admin/figma/sync`                                               | 인증 필요     | 활성 파일 전체 즉시 동기화                                 |
| POST  | `/api/v1/admin/figma/sync/watched-files/{id}`                            | 인증 필요     | 특정 파일 즉시 동기화 (enabled 무관)                       |
| GET   | `/api/v1/admin/figma/sync/watched-files/{id}/preview`                    | 인증 필요     | 신규 댓글 + LLM 분류/매칭 결과 미리보기 (발송/저장 X)             |

## 10. 환경 변수

`application.yml` 의 `app.llm.*` + `app.figma.*` 키.

```yaml
app:
  llm:
    provider: ${LLM_PROVIDER:mock}      # mock | openai | gemini | spring-ai (후속 PR)

  figma:
    oauth:
      client-id: ${FIGMA_OAUTH_CLIENT_ID:}
      client-secret: ${FIGMA_OAUTH_CLIENT_SECRET:}
      redirect-uri: ${FIGMA_OAUTH_REDIRECT_URI:http://localhost:8080/api/v1/admin/figma/oauth/callback}
      scope: ${FIGMA_OAUTH_SCOPE:current_user:read,file_metadata:read,file_comments:read}
      authorize-uri: https://www.figma.com/oauth
      token-uri: https://api.figma.com/v1/oauth/token
      refresh-uri: https://api.figma.com/v1/oauth/refresh
      token-encryption-key: ${FIGMA_TOKEN_ENCRYPTION_KEY:umc-product-figma-default-key}
    sync:
      enabled: ${FIGMA_SYNC_ENABLED:false}
      poll-interval: ${FIGMA_SYNC_POLL_INTERVAL:PT5M}
      max-files-per-run: ${FIGMA_SYNC_MAX_FILES_PER_RUN:50}
```

- `FIGMA_TOKEN_ENCRYPTION_KEY` 는 운영 환경에서 반드시 별도 시크릿으로 주입할 것.
- `LLM_PROVIDER` 는 본 시점 `mock` 만 사용하며, OpenAI/Gemini/Spring AI 어댑터를 추가하는 PR 에서 키와 인증 정보가 함께 추가될 예정.

## 11. 트레이드오프 / 알려진 한계

| 항목                       | 현재 구현                                | 한계 / 후속 과제                                                                  |
|--------------------------|--------------------------------------|----------------------------------------------------------------------------|
| 폴링 주기                    | 고정 5분 (`PT5M`)                       | 댓글이 즉시 도달하지 않음 — Figma rate limit 동시 고려                                     |
| OAuth state 저장소          | in-memory `ConcurrentHashMap`        | 다중 인스턴스 환경에서는 sticky session 또는 Redis 로 교체 필요                                |
| 토큰 암호화                   | application property 기반 AES-GCM      | KMS / Jasypt 로 보강 여지                                                       |
| 다중 인스턴스 스케줄러 중복 실행 방지     | 별도 락 없음 (다른 스케줄러도 동일 컨벤션)             | 다중 인스턴스 운영 가시화 시 ShedLock 등 분산 락 도입                                          |
| 페이지명 캐시                  | 없음 (필요 시마다 Figma REST 호출)            | Caffeine 캐시 도입 여지 — 폴링 부하 가시화 시 검토                                            |
| LLM provider             | mock 만 활성. 분류 정확도는 흐름 검증 수준          | OpenAI / Gemini / Spring AI 어댑터를 동일 인터페이스로 추가, prompt cache 도입 검토            |
| LLM 비용/지연                | 신규 댓글마다 + preview 호출마다 LLM 1회 호출      | provider 도입 시 호출 batch / 분류 결과 단기 캐시 검토                                      |
| Discord 멘션 권한            | webhook 자체에 mention 권한 부여 가정         | 권한 미부여 시 멘션 사일런스 — webhook 생성 시 채널 설정 확인 필요                                  |
| Discord embed 길이 제한      | 어댑터에서 description ≤ 4096 자, field value ≤ 1024 자 truncate | Discord 제약 (embed 1건 ≤ 6000 자) 안에서 동작. 댓글이 매우 긴 경우 본문이 잘릴 수 있음 |

## 12. 디버깅 체크리스트

| 증상                            | 원인 후보 / 확인 위치                                                                                       |
|-------------------------------|----------------------------------------------------------------------------------------------------|
| Discord 채널에 아무것도 안 옴           | `app.figma.sync.enabled=true`, `watched_file.enabled=true`, 라우팅 도메인 1건 이상 등록 (없으면 ROUTING_DOMAIN_NOT_REGISTERED 로 sync skip) |
| 모든 댓글이 fallback 으로 감           | LLM 응답이 항상 후보 외 → provider=mock 인지, system prompt 가 모델에 잘 전달됐는지, 후보 도메인 키가 사람이 봐도 의미가 명확한지 점검         |
| 일부 댓글만 fallback 으로 감           | LLM 분류 모호. preview 로 댓글별 결과 확인, `description` 보강 / domain_key 명명 재검토                                  |
| 멘션이 안 되고 embed 만 보임           | `allowed_mentions.parse=["roles","users"]` 적용 확인, mention 이 embed 본문이 아니라 외부 `content` 영역에 들어가는지 확인 |
| Discord embed 색상/필드 누락         | 어댑터의 embed 빌드 로그 확인 (`Discord embed 멘션 전송 완료`)                                                       |
| OAuth callback 에서 NPE/Unauthorized | `/start` 와 `/callback` 사이 10분 초과 / 이미 사용된 state / 서버 재시작 → state 무효 → `/start` 부터 재시도              |
| `last_error` 에 401 누적          | refresh token 만료 또는 회수 → 운영진 재인증                                                                   |
| `last_error` 에 429 누적          | Figma rate limit — 폴링 주기 늘리거나 max-files-per-run 줄임                                                |
| node_id 없는 댓글에서 NPE            | (수정 완료) `Map.of()` 가 null key 를 거부 → null 가드 적용된 상태                                               |

## 13. 참고

- ADR: [docs/adr/003-figma-comment-discord-forwarder.md](../adr/003-figma-comment-discord-forwarder.md)
- Figma API: [OAuth2](https://www.figma.com/developers/api#oauth2), [Get comments](https://www.figma.com/developers/api#get-comments-endpoint), [Get file](https://www.figma.com/developers/api#get-files-endpoint)
- Discord API: [Execute Webhook](https://discord.com/developers/docs/resources/webhook#execute-webhook), [Allowed Mentions](https://discord.com/developers/docs/resources/channel#allowed-mentions-object)
