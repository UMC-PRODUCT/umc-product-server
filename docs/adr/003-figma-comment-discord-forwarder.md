# ADR-003: Figma 파일 댓글을 OAuth 기반으로 폴링해 담당 파트별 Discord 멘션으로 전달한다

## Status

Amended (2026-05-07, 1차): 4·5·7번 결정을 LLM 분류 + 도메인 라우팅 + Discord embed 포맷 + LLM 도메인 분리로 변경. 자세한 사유는 본문 및 `Alternatives Considered §3` 의 "결정 번복 사유" 참조.

Amended (2026-05-07, 2차): 5번 결정의 발송 단위를 "댓글 1건 = Discord 호출 1회" 에서 **"도메인 1건 = 묶음 embed 메시지 1건"** 으로 변경. Discord rate limit 부담 완화 + 가독성 개선 목적. 같은 사이클 + 같은 라우팅 도메인의 댓글이 cross-file 로 한 메시지에 담긴다. 운영진의 임의 시간창 catch-up 을 위한 `POST /admin/figma/digest?from&to` API 도 함께 도입. 자세한 사유는 본문 `Decision §5` 의 "2차 amendment" 와 `Alternatives Considered §6` 참조.

Partially superseded (2026-05-08): 3번 결정 (`comment.id` 기반 중복 방지) 은 [ADR-004](004-figma-comment-time-window-unification.md) 의 시간창 + `figma_summary_cursor` + `figma_comment_dispatch` 가드로 대체된다. 1·2·4·5·6·7번 결정 (OAuth, 폴링, LLM 분류, 도메인 라우팅, embed 포맷, LLM 도메인 분리, digest API) 은 그대로 유효하다.

## Context

UMC PRODUCT 운영진은 디자인 리뷰와 기획 검토 과정에서 Figma 파일에 달리는 댓글(comment)을 통해 의사소통을 한다. 그러나 현재는 다음과 같은 운영상 문제가 있다.

1. **알림 누락**: Figma 기본 알림은 멘션된 사람 또는 watcher에게만 도달한다. 담당 파트장(예: 디자인 파트장, 기획 파트장)이 Figma에 watcher로 등록되어 있지 않거나 알림을 끈 경우, 댓글이 누락되어 회신이 늦어진다.
2. **소통 채널 분리**: 팀 내 비동기 커뮤니케이션은 Discord에 집중되어 있으나, 디자인 피드백은 Figma에 고립되어 있어 두 채널을 병행 확인해야 한다.
3. **담당자 라우팅 부재**: 댓글 내용 자체가 어느 파트(디자인/기획/개발)에 해당하는지 사람이 매번 판단해야 하며, 다수의 파일이 있을 경우 운영진의 인지 부담이 크다.

이 문제를 해결하기 위해, 특정 Figma 파일 목록에 달린 댓글을 서버가 주기적으로 가져와 Discord로 전달하되, **댓글이 지정된 노드/페이지의 담당 파트를 기준으로 Discord에서 해당 파트 역할(role)을 멘션**하는 시스템이 필요하다.

기술적 제약은 다음과 같다.

- Figma의 댓글 변경 이벤트 webhook은 Enterprise 플랜에서만 제공되며 현 팀 플랜으로는 사용 불가하다. REST API 폴링이 사실상 유일한 선택지다.
- Figma REST API의 `GET /v1/files/:file_key/comments`는 OAuth2 access token 또는 personal access token으로 호출 가능하다. 다만 personal access token은 발급한 개인 계정의 권한과 운명을 같이 하므로, 해당 인원이 팀에서 빠지면 토큰이 사라진다.
- 서버는 이미 [DiscordWebhookAdapter](../../src/main/java/com/umc/product/notification/adapter/out/external/webhook/DiscordWebhookAdapter.java)를 통해 Discord 메시지 전송 인프라를 보유하고 있다.
- 파트 enum은 이미 [ChallengerPart](../../src/main/java/com/umc/product/common/domain/enums/ChallengerPart.java)로 존재하지만, 이 enum은 챌린저(수강생) 파트 분류용이므로 Figma 댓글 라우팅에 그대로 재사용하면 의미가 흐려진다.
- Spring Security의 `spring-security-oauth2-client`는 이미 의존성으로 포함되어 있고, Google/Kakao 로그인 흐름에서 사용 중이다. 그러나 그 흐름은 **사용자 로그인**이므로, Figma 운영자의 위임(delegation) OAuth와는 사용 목적이 다르다.

따라서 이번 결정에서는 다음을 정해야 한다.

- Figma 인증 방식 (OAuth2 vs Personal Access Token)
- 댓글 수집 트리거 (Polling vs Webhook vs 사용자 트리거)
- 담당 파트 분류 규칙 (메시지 파싱 vs 노드 위치 기반 vs 페이지 레이블 기반)
- Discord 멘션 방식 (자유 텍스트 vs Role ID 기반)
- 도메인 배치 (기존 `notification` 도메인에 흡수 vs 신규 `figma` 도메인 신설)

## Decision

우리는 다음과 같이 결정한다.

1. **인증**: Figma OAuth2 Authorization Code Flow를 사용한다. 운영진 1인이 최초 1회 동의 화면을 통과해 발급받은 refresh token을 서버에 저장하고, 이후 서버가 access token을 자동 갱신한다.
2. **트리거**: 고정 주기 폴링(예: 5분)을 사용한다. 이미 운영 중인 Spring `@Scheduled` 인프라를 활용하며, 폴링은 등록된 Figma 파일 단위로 수행한다.
3. **중복 방지**: 파일별 마지막으로 처리한 `comment.id`(또는 `created_at`)를 DB에 저장하고, 이후 폴링에서 그보다 새로운 댓글만 Discord로 전달한다.
4. **분류 규칙 (Amended)**: 댓글 본문을 LLM 으로 분석해 운영진이 등록한 라우팅 도메인 키(`figma_routing_domain.domain_key`) 중 하나로 분류한다. 후보 도메인 키 외 응답 / LLM 호출 실패 / 매칭 미스 시 `fallback=true` 인 도메인으로 보낸다. 페이지 이름은 라우팅 키가 아니라 embed 의 부가 정보로만 사용한다.
5. **멘션 + 메시지 포맷 (Amended, 2026-05-07 2차)**:
   - 도메인 단위로 멘션 대상 (Discord role 또는 user) 을 N개까지 영속화(`figma_routing_domain_mention`)한다. 멘션은 알림 발생을 위해 메시지의 외부 `content` 영역에 출력한다.
   - **발송 단위는 "도메인 1건"** 이다. 한 사이클(또는 digest 시간창) 안의 같은 라우팅 도메인 댓글들은 cross-file 로 한 메시지의 embed 안에 모인다.
   - embed 의 `fields[]` 에 댓글 1건 = field 1개 (작성자 · 파일/페이지 + 본문 + 댓글 deeplink). Discord 제약 (fields 25/embed, embeds 10/message) 에 맞춰 어댑터가 자동 분할 발송한다.
   - 운영진의 임의 시간창 catch-up 을 위해 `POST /admin/figma/digest?from={Instant}&to={Instant}` 를 함께 노출한다 (sync state 비변경, 동일 시간창 반복 호출 가능).
6. **도메인 배치**: 신규 `figma` 도메인을 신설한다(`com.umc.product.figma`). 외부 시스템(Figma)을 1차 시민으로 다루는 별도 컨텍스트이며, 기존 `notification` 도메인은 "범용 알림 발송 인프라"로 두고, `figma` 도메인이 그 발송 포트(`SendWebhookPort` 또는 신규 포트)를 호출하는 구조로 분리한다.
7. **LLM 도메인 분리 (Added)**: LLM 호출 추상은 별도 도메인 `com.umc.product.llm` 으로 분리한다. `ChatCompletionPort` 를 두고 provider 별 구현체(Mock / OpenAI / Gemini / Spring AI)를 `app.llm.provider` 로 교체할 수 있도록 한다. 본 ADR 시점에는 `MockChatCompletionAdapter` 만 활성화되어 후보 중 무작위 응답을 반환하며, 실제 모델 도입은 별도 PR 에서 진행한다.

## Alternatives Considered

### 1. Personal Access Token (PAT) 사용

운영진 1인이 본인 계정에서 PAT를 발급해 서버에 직접 주입하는 방식이다.

장점:

- OAuth 콜백 라우팅, 토큰 갱신, 동의 화면이 필요 없어 구현이 가장 단순하다.
- Figma OAuth 앱 등록 절차가 생략된다.

단점:

- 발급한 운영진이 팀에서 이탈하면 즉시 모든 Figma 연동이 중단된다.
- PAT는 권한 범위 제어가 약하고, 노출 시 해당 계정 전체 권한이 노출된다.
- 운영진 교체 시 수동 PAT 재발급/재배포가 필요하다.

선택하지 않은 이유:
운영진 인수인계 비용과 보안 위험이 OAuth 도입 비용보다 크다. OAuth는 동의자가 바뀌어도 같은 절차로 재인증이 가능하며, scope 단위 권한 부여가 가능하다.

### 2. Figma Webhooks v2

Figma가 제공하는 webhook을 통해 댓글 이벤트를 즉시 수신하는 방식이다.

장점:

- 폴링 비용 없이 거의 실시간 알림이 가능하다.
- 누락 위험이 낮다(Figma가 재시도까지 보장).

단점:

- `FILE_COMMENT` webhook은 Organization/Enterprise 플랜에서만 활성화된다. 현재 팀 플랜에서 사용 불가다.
- webhook 수신 엔드포인트를 외부에 노출해야 하며, signature 검증과 retry 정책 구현이 추가된다.

선택하지 않은 이유:
플랜 자체가 부족해 사용할 수 없다. 추후 Enterprise로 업그레이드되면 ADR을 superseding하는 방향으로 재검토한다.

### 3. 자연어 기반 파트 분류 (LLM 또는 키워드 매칭)

댓글 본문을 LLM 또는 키워드 사전으로 분석해 담당 파트를 추론하는 방식이다.

장점:

- 사용자가 댓글 다는 위치/맥락에 자유롭다.
- 매핑 테이블 관리 비용이 없다.

단점:

- 잘못 분류될 경우 담당이 아닌 파트가 멘션을 받게 되어, 알람 신뢰도가 떨어진다.
- LLM 호출은 비용·지연·외부 의존성을 추가한다.
- "디자인 파일에 달린 댓글이지만 기획 의도 질문"처럼 본문만으로는 분류가 모호한 케이스가 빈번하다.

선택하지 않은 이유:
이번 시스템의 1차 목적은 "담당자에게 정확히 도달시키는 것"이며, 분류 오류는 시스템 신뢰를 즉시 무너뜨린다. Figma 페이지/프레임 이름 컨벤션은 팀 내에서 이미 통제 가능한 수단이므로, 명시적 매핑이 더 견고하다.

#### 결정 번복 사유 (Amended 2026-05-07)

운영 검토 결과 위 결정을 뒤집고 LLM 분류 + 도메인 라우팅을 채택했다. 사유:

- **페이지 컨벤션 강제의 한계**: Figma 파일/페이지명은 디자이너가 자유롭게 변경하는 자산이라 라우팅 키로 강제하기 어려웠다. 페이지 이름이 바뀔 때마다 운영진이 매핑을 갱신해야 하는 비용이 LLM 호출 비용보다 컸다.
- **댓글이 페이지가 아니라 "어떤 도메인 작업" 인지가 더 중요**: "로그인 화면" 페이지에 달린 댓글이라도 실제 책임은 인증/회원/UI 등 서버 도메인 단위로 분기된다. 페이지 → 파트 매핑은 이 차이를 표현하기 부적절했다.
- **분류 오류 리스크는 fallback + preview 로 흡수 가능**: LLM 응답이 후보 외 값이거나 실패면 fallback 도메인으로 라우팅하고, preview API 로 사전 검증할 수 있다. 신뢰가 무너질 정도의 리스크는 아니라고 판단.
- **확장성**: 추후 Notion / Slack 등 다른 외부 댓글 시스템도 같은 LLM 도메인을 재사용해 동일 분류기로 라우팅할 수 있다.

신뢰성 보강을 위해 LLM 분류 결과 fallback 매칭, mock provider 우선 도입, embed 의 Domain 필드로 운영진이 분류 결과를 시각적으로 검증할 수 있게 했다.

### 4. 자유 텍스트 파트명만 메시지에 포함 (Role 멘션 없이)

Discord 메시지 본문에 "디자인 파트" 같은 평문만 적고, role mention은 사용하지 않는 방식이다.

장점:

- Role ID 관리가 필요 없다.
- 잘못 멘션해 무관한 인원이 핑되는 사고가 없다.

단점:

- 평문은 Discord에서 알림을 발생시키지 않는다. 결국 채널을 직접 보고 있어야만 인지가 가능해, 도입 목적(알림 누락 해소)이 무력화된다.

선택하지 않은 이유:
멘션 없는 알림은 이번 ADR의 동기와 정면으로 충돌한다.

### 6. 댓글 1건 = Discord 호출 1회 유지 (2026-05-07 2차 amendment 에서 번복)

이전 amendment 까지의 결정. 신규 댓글마다 즉시 webhook 호출.

장점:

- 댓글 단위 발송 실패 격리가 명료하다 (한 댓글 실패가 다른 댓글 발송에 영향 없음).
- 구현이 단순하고 추적/로깅이 직관적이다.

단점:

- 댓글 K 건이면 webhook 호출도 K 회. Discord webhook rate limit (per-URL 약 5 req / 2s, 30 req / 분) 에 닿기 쉽고, 도메인의 채널이 한 사이클에 수십 개의 알림으로 가득 찬다.
- 받는 사람 입장에서 같은 사이클의 관련 댓글이 시간순으로 흩어져 가독성이 떨어진다.
- 멘션이 댓글마다 발생하므로 알림 피로도가 누적된다.

선택하지 않은 이유 (2차 amendment):
운영 검토 결과 (1) Discord rate limit 부담과 (2) 채널 가독성 저하가 운영진 신뢰를 떨어뜨리는 1차 요인으로 식별됐다. 발송 단위를 "사이클 + 도메인" 으로 묶으면 호출수가 급감하고 (전형적 5분 사이클 K<25 시 N개 도메인이면 N개 메시지), embed `fields[]` 로 댓글 모음을 한 곳에 정리할 수 있다. 발송 실패 격리는 "도메인 묶음 단위" 로 약화되지만, ADR-003 초기 결정대로 발송 실패 시 `last_synced_comment_id` 를 그대로 advance 하는 정책이 유지되므로 운영 영향은 동일 범위.

### 5. 기존 `notification` 도메인에 흡수

새 도메인을 만들지 않고, 기존 `notification` 도메인 안에 Figma 폴링 로직과 라우팅 룰을 추가하는 방식이다.

장점:

- 신규 패키지/마이그레이션이 줄어든다.
- 알림 발송 책임이 한곳에 모인다.

단점:

- `notification` 도메인의 Aggregate가 "외부 시스템 댓글 동기화 상태(`figma_sync_state`)" 같은, 알림 발송과 무관한 책임을 갖게 되어 응집도가 낮아진다.
- 추후 Notion, Slack 등 다른 외부 소스를 추가하면 `notification` 도메인이 모든 외부 시스템의 폴링/상태 저장소를 떠안게 된다.

선택하지 않은 이유:
`notification` 도메인은 "어떤 채널로 어떤 알림을 발송하는가"의 경계로 두고, "외부 시스템에서 무엇을 가져오는가"는 별도 도메인(`figma`)에 두는 것이 향후 확장성과 응집도 측면에서 유리하다.

## Consequences

### Positive

- Figma 댓글이 누락 없이 Discord에서 담당 파트에게 도달하므로, 디자인/기획 피드백 회수 시간이 줄어든다.
- OAuth 기반이므로 운영진 교체 시 PAT 재발급/재배포 없이 동의만 다시 받으면 된다.
- 폴링 주기, 모니터링 대상 파일, 파트-페이지 매핑이 모두 DB/설정으로 분리되어 코드 변경 없이 운영 조정이 가능하다.
- 신규 `figma` 도메인이 외부 시스템 통합의 표준 패턴이 되어, 추후 Notion/Slack 등 동일 구조 통합이 쉬워진다.

### Negative

- 폴링 간격 사이에 발생한 댓글은 즉시 도달하지 않는다(최대 폴링 주기만큼 지연).
- Figma OAuth 동의자 한 명에게 토큰 관리 책임이 집중된다. 해당 운영진의 Figma 권한이 회수되면 시스템 전체가 멈춘다.
- 매핑 테이블(`figma_part_route`)을 운영팀이 명시적으로 관리해야 한다. 새 페이지가 추가되었는데 매핑이 없으면 fallback 채널로만 알림이 가서 담당자 누락이 발생할 수 있다.
- Figma API rate limit (분당 요청 수 제한)에 도달하지 않도록 파일 수 × 폴링 주기를 통제해야 한다.

### Neutral / Trade-offs

- refresh token을 DB에 평문으로 저장할지, KMS/Jasypt 등으로 암호화할지는 별도 결정 사항이다. 본 ADR에서는 application property 기반 대칭키로 암호화하는 안을 기본으로 두되, 운영 정책 결정 후 보강한다.
- 멘션 텍스트가 Discord 메시지에 포함되므로, Discord webhook이 `allowed_mentions` 정책을 사용할 경우 별도 설정이 필요하다. 본 ADR에서는 `allowed_mentions.parse`에 `roles`를 포함하는 것을 기본 정책으로 한다.
- 폴링 주기를 짧게 가져갈수록 알림 지연은 줄지만 Figma API 호출량은 늘어난다. 5분을 기본으로 하되, 운영 데이터 확보 후 조정한다.

## Implementation Notes

### 도메인 패키지 구조 (신규)

```
com.umc.product.figma/
├── domain/
│   ├── FigmaIntegration.java          # OAuth refresh token 등 통합 상태
│   ├── FigmaWatchedFile.java          # 폴링 대상 파일과 sync 상태
│   ├── FigmaPartRoute.java            # 페이지/프레임 이름 → 파트 role 매핑
│   ├── FigmaCommentSnapshot.java      # 가장 최근 처리된 comment 식별자
│   └── exception/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── SyncFigmaCommentsUseCase.java         # 스케줄러 진입점
│   │   │   ├── RegisterFigmaIntegrationUseCase.java  # OAuth callback 처리
│   │   │   └── ManageFigmaWatchedFileUseCase.java    # 파일 등록/해제
│   │   └── out/
│   │       ├── LoadFigmaIntegrationPort.java
│   │       ├── SaveFigmaIntegrationPort.java
│   │       ├── LoadFigmaWatchedFilePort.java
│   │       ├── SaveFigmaWatchedFilePort.java
│   │       ├── LoadFigmaPartRoutePort.java
│   │       ├── FetchFigmaCommentPort.java     # Figma REST 호출
│   │       └── SendDiscordMentionPort.java    # 또는 기존 SendWebhookPort 재사용
│   └── service/
│       ├── FigmaIntegrationCommandService.java
│       ├── FigmaWatchedFileCommandService.java
│       └── FigmaCommentSyncCommandService.java
└── adapter/
    ├── in/
    │   ├── web/    # OAuth callback, 파일 등록 admin API
    │   └── scheduler/ # FigmaCommentSyncScheduler
    └── out/
        ├── persistence/
        └── external/
            ├── FigmaOAuthClient.java
            ├── FigmaCommentClient.java
            └── FigmaOAuthProperties.java
```

### 주요 환경변수 (Amended)

```yaml
app:
  llm:
    provider: ${LLM_PROVIDER:mock}   # mock | openai | gemini | spring-ai (후속 PR)

  figma:
    oauth:
      client-id: ...
      client-secret: ...
      redirect-uri: https://api.umc-product.com/admin/figma/oauth/callback
      scope: current_user:read,file_metadata:read,file_comments:read
      authorize-uri: https://www.figma.com/oauth
      token-uri: https://api.figma.com/v1/oauth/token
      refresh-uri: https://api.figma.com/v1/oauth/refresh
      token-encryption-key: ${FIGMA_TOKEN_ENCRYPTION_KEY}
    sync:
      enabled: ${FIGMA_SYNC_ENABLED:false}
      poll-interval: PT5M
      max-files-per-run: 50
```

> 이전 안에 있던 `app.figma.discord.role-mentions` (도메인별 role ID 를 yaml 에 평문으로 두는 안) 은 폐기되었다.
> Discord webhook URL / 멘션 ID 는 모두 DB(`figma_routing_domain`, `figma_routing_domain_mention`) 에 행 단위로 보관되며,
> admin API 로 등록/삭제한다.

### 데이터 모델 (Flyway, Amended)

- `figma_integration` (id, owner_member_id, refresh_token_enc, access_token_enc, access_token_expires_at, scope, created_at, updated_at)
- `figma_watched_file` (id, file_key, display_name, enabled, last_synced_comment_id, last_synced_at, last_error, created_at, updated_at)
- ~~`figma_part_route`~~ — 폐기됨 (V2026.05.07.10.20 에서 DROP)
- `figma_routing_domain` (id, domain_key UNIQUE, description, discord_webhook_url, fallback BOOLEAN, created_at, updated_at)
- `figma_routing_domain_mention` (id, domain_id FK, mention_id, mention_type ENUM[ROLE/USER], display_label, created_at, updated_at)

### LLM 분류 도메인

별도 도메인 `com.umc.product.llm` 으로 분리.

- `ChatCompleteUseCase` (in) / `ChatCompletionPort` (out) / `ChatCompletionService`
- `ChatCompleteCommand`: `systemPrompt` + `userPrompt` + `candidates` 리스트 (분류 모드)
- 어댑터는 `@ConditionalOnProperty(app.llm.provider=...)` 로 단일 활성화
  - `MockChatCompletionAdapter` (provider=mock, 본 ADR 시점 기본): candidates 중 무작위 하나 반환
  - 후속 PR: OpenAI / Gemini / Spring AI 어댑터 동일 인터페이스로 추가

### Figma REST 호출

- 댓글 조회: `GET https://api.figma.com/v1/files/:file_key/comments` (Authorization: `Bearer {access_token}`)
- 댓글 위치: 응답의 `client_meta.node_id`. 페이지 이름은 `GET /v1/files/:file_key/nodes?ids=...` 의 ancestors 에서 type=CANVAS 노드명을 추출해 embed 의 부가 정보로 사용 (라우팅 키로는 사용하지 않음).
- access token 갱신: `POST https://api.figma.com/v1/oauth/refresh` (refresh token 사용)

### Discord 메시지 포맷 (Amended 2026-05-07 2차 — 도메인 묶음 embed)

같은 사이클(또는 digest 시간창) + 같은 라우팅 도메인의 댓글을 cross-file 로 한 메시지에 모은다.

```jsonc
{
  "content": "<@&123> <@456>",                         // 멘션만, 알림 발생 영역 (첫 페이지 메시지에만 포함)
  "embeds": [{
    "title": "[Figma] {domainKey} 신규 댓글 {N}건",     // 페이지 분할 시 " (1/3)" 등의 접미사
    "color": 15879710,                                  // 0xF24E1E
    "fields": [                                         // 댓글 1건 = field 1개, 최대 25
      {
        "name": "👤 {작성자} · {파일} / {페이지명}",
        "value": "{댓글 본문}\n🔗 [열기]({deeplink})",
        "inline": false
      },
      // ... 더 많은 댓글
    ],
    "footer": {"text": "Figma · {windowFrom} ~ {windowTo}"},
    "timestamp": "{묶음 내 최신 댓글 시각, ISO8601}"
  }],
  "allowed_mentions": {"parse": ["roles", "users"]}
}
```

Discord 제약 (fields 25/embed, embeds 10/message, embed 합산 ≤ 6000자) 초과 시 어댑터가 자동으로 메시지를 분할 발송한다. 첫 페이지 메시지의 `content` 에만 멘션을 포함해 알림이 한 번만 울리도록 한다.

### digest API 형식 (Added 2026-05-07 2차)

```
POST /api/v1/admin/figma/digest?from={Instant}&to={Instant}
```

- 사이클 / sync 상태와 무관하게 [from, to] 안의 댓글을 도메인별로 묶어 Discord 로 발송한다.
- `last_synced_comment_id` 를 변경하지 않으므로 동일 시간창을 반복 호출하면 Discord 에 다시 발송된다 (catch-up / 회고용).
- 응답: `FigmaDigestSummary { from, to, totalComments, unmatchedCount, domains[] }`. domains[].sent 가 발송 성공 여부.

### 분류 → 라우팅 → 묶음 발송 흐름 (Amended)

```
1. enabled 파일 목록 → 파일별로 comments 조회 (Figma REST)
2. 파일별로 시간창 적용:
   - sync     : last_synced_comment_id 이후의 댓글
   - digest   : createdAt 이 [from, to] 인 댓글
3. LLM candidates = SELECT domain_key FROM figma_routing_domain
4. 댓글 1건씩 ChatCompleteCommand.classify 호출 → domain_key
5. 응답이 candidates 에 포함되면 매칭 도메인, 아니면 fallback 도메인
6. 모든 (file, comment, applied_domain) 튜플을 도메인 단위로 group by
7. 도메인별로:
   - mentions = SELECT * FROM figma_routing_domain_mention WHERE domain_id = applied
   - embed payload 빌드 (fields 25/embed, embeds 10/message 자동 분할)
   - webhook POST
8. (sync 만) 파일별 last_synced_comment_id 를 묶음 내 최신 commentId 로 advance
   (digest 는 비변경)
```

### 운영 시 주의사항

- 최초 OAuth 동의는 운영진 1인이 진행한다. 동의자 변경은 `RegisterFigmaIntegrationUseCase`를 다시 호출하면 덮어쓴다.
- `figma_watched_file.last_synced_comment_id`는 절대 UPDATE를 누락하면 안 된다. 누락 시 다음 폴링에서 동일 댓글이 중복 발송된다. 폴링 처리 트랜잭션 안에서 같이 갱신한다.
- Figma rate limit 초과 시 `429`를 받으므로, 어댑터 레벨에서 Retry-After를 존중하고 다음 주기로 미룬다(즉시 재시도 금지).
- LLM 분류가 후보 외 값을 반환하거나 호출이 실패하면 자동으로 fallback 도메인으로 라우팅된다. 운영진은 정기적으로 fallback 채널을 확인해 분류 정확도를 점검한다.
- LLM 호출은 비용/지연을 발생시키므로 신규 댓글에 대해서만 호출한다 (이미 last_synced_comment_id 이전 댓글은 호출하지 않음). 향후 후보 도메인 변경 빈도가 낮다면 prompt 캐시 도입 여지가 있다.
- preview API 도 LLM 호출을 발생시키므로 운영진의 preview 사용 빈도가 비용 변수가 된다.

## Implementation Plan (Commit 단위)

각 커밋은 단독으로 빌드/테스트가 통과해야 하며, Conventional Commits 규칙(`<type>: <subject>`)을 따른다. PR은 의미 단위로 묶어 `[Feat] Figma 댓글 Discord 포워딩 …` 등의 제목을 사용한다.

> **2026-05-07 1차 Amendment**: 아래 1~10번은 1차 구현(페이지명 기반 라우팅) 의 커밋 계획이며 이미 머지되었다.
> 그 위에 LLM 분류 + 도메인 라우팅 + Discord embed 전환을 다음 7건 커밋으로 추가했다:
>
> 1. `chore: LLM 도메인 패키지와 ChatCompletion 추상 추가`
> 2. `feat: LLM mock 어댑터 랜덤 응답 구현`
> 3. `feat: figma 라우팅 도메인/담당자 데이터 모델 추가`
> 4. `feat: figma 라우팅 도메인/담당자 admin API 추가`
> 5. `feat: figma 댓글 LLM 기반 도메인 분류 적용 및 page_name 라우팅 제거`
> 6. `feat: Discord 멘션 embed 포맷 도입`
> 7. `docs: ADR-003 / 업무보고서 LLM 라우팅 기반으로 갱신`
>
> **2026-05-07 2차 Amendment**: 댓글 단위 발송 → 도메인 단위 묶음 embed + digest API 도입을 다음 4건 커밋으로 추가했다:
>
> 1. `feat: figma 댓글 동기화를 도메인 단위 batch embed 발송으로 전환`
> 2. `feat: from/to 기반 figma digest admin API 추가`
> 3. `feat: figma 댓글 preview 응답을 도메인 묶음 형태로 정렬`
> 4. `docs: ADR-003 / 업무보고서 도메인 묶음 발송 + digest API 반영` (본 변경)

1. `chore: figma 도메인 패키지와 환경 설정 스켈레톤 추가`
   - `com.umc.product.figma` 하위 빈 패키지 생성, `FigmaOAuthProperties`(record) 추가, `application.yml`/`application-local.yml`에 `app.figma.*` 키 추가, `RestClient` 빈 재사용 확인.
   - 외부 호출 없음. 단순 스캐폴딩.

2. `feat: figma 통합 도메인과 Flyway 마이그레이션 작성`
   - `FigmaIntegration`, `FigmaWatchedFile`, `FigmaPartRoute` 엔티티/도메인 객체와 Builder, 도메인 메서드(`rotateAccessToken`, `markSynced`, `disable` 등) 추가.
   - Flyway: `V2026.MM.DD__create_figma_tables.sql` (3개 테이블, 인덱스 `figma_watched_file(enabled)`, `figma_part_route(file_key, page_name)` UNIQUE).
   - JPA Repository(`FigmaIntegrationRepository` 등), Persistence Adapter, Save/Load Port 작성.

3. `feat: figma OAuth authorization code 교환 및 refresh 흐름 구현`
   - `FigmaOAuthClient`(adapter/out/external)에서 token endpoint 호출.
   - `RegisterFigmaIntegrationUseCase` + `FigmaIntegrationCommandService`: authorization code → access/refresh token 저장.
   - access token 만료 시 자동 refresh하는 `RefreshFigmaAccessTokenPort` 또는 어댑터 내부 메서드.
   - 토큰 평문 저장 금지: `Jasypt` 또는 application key 기반 대칭 암호화 유틸 적용.

4. `feat: figma OAuth 콜백과 watched file 관리 admin API 추가`
   - `FigmaOAuthController`(adapter/in/web): `GET /admin/figma/oauth/start` (state 생성·동의 URL 리다이렉트), `GET /admin/figma/oauth/callback`.
   - `FigmaWatchedFileController`: 운영진이 file_key를 등록/해제하는 관리 API. `@PreAuthorize` 또는 admin role 가드 필수.
   - 입력 DTO는 `record` + `@Valid`로 검증.

5. `feat: figma 댓글 조회 및 페이지명 해석 어댑터 구현`
   - `FigmaCommentClient`: `GET /v1/files/:file_key/comments` 호출, `client_meta.node_id`까지 매핑한 `FigmaCommentInfo` 반환.
   - `FigmaFileMetadataClient`: 파일 메타에서 노드 ID → 페이지 이름 해석 로직. 결과는 짧은 in-memory 캐시(`Caffeine`).
   - 429/401 처리: 401이면 refresh 1회 시도 후 재요청, 429면 그대로 예외 throw.

6. `feat: figma 댓글 동기화 유즈케이스 및 파트 라우팅 구현`
   - `SyncFigmaCommentsUseCase` / `FigmaCommentSyncCommandService`(`@Transactional`).
   - 단계: enabled 파일 조회 → 파일별 access token 확보 → 마지막 sync 이후 댓글만 필터 → 페이지명 해석 → `FigmaPartRoute`로 파트/role 결정 → Discord 발송 → `last_synced_comment_id` 갱신.
   - 매핑 미스 시 fallback route 사용. 발송 실패 시 트랜잭션은 롤백하지 않고 `last_error`만 기록(중복 발송 방지 우선).

7. `feat: 파트 멘션 가능한 Discord 발송 포트와 어댑터 추가`
   - `SendDiscordMentionPort` 신규 포트와 `DiscordMentionWebhookAdapter`. 입력으로 webhook URL, role ID, embed payload를 받음.
   - `allowed_mentions.parse=["roles"]` 명시.
   - 기존 `DiscordWebhookAdapter`는 그대로 유지(범용 알림용).

8. `feat: figma 댓글 폴링 스케줄러 등록`
   - `FigmaCommentSyncScheduler`(adapter/in/scheduler): `@Scheduled(fixedDelayString = "${app.figma.sync.poll-interval}")`.
   - 다중 인스턴스 환경 대비를 위해 `ShedLock` 사용(이미 사용 중인지 확인 후 결정. 없다면 본 커밋에서 의존성 추가).
   - local profile에서는 비활성화 옵션(`app.figma.sync.enabled=false`).

9. `test: figma 동기화 및 OAuth 흐름 단위/통합 테스트 추가`
   - `FigmaCommentSyncCommandServiceTest`(Mockito): 신규 댓글만 필터링, 매핑 미스 시 fallback, 409/429 처리.
   - `FigmaOAuthClientTest`: `MockRestServiceServer`로 token/refresh 응답 검증.
   - Testcontainers 기반 `FigmaPersistenceAdapterTest`: 마이그레이션, repository 동작 검증.
   - 모두 `@DisplayName`은 한국어, Given/When/Then 구조.

10. `docs: figma 댓글 포워딩 운영 가이드 작성 (선택)`
    - `docs/guides/figma-comment-forwarder.md`: OAuth 동의 절차, 파일 등록 방법, 매핑 추가 방법, Discord role ID 확보 방법, 장애 대응 체크리스트.
    - 본 ADR과 상호 링크.

## References

- 관련 ADR
    - [ADR-001: Apple 로그인 ClientType 라우팅](001-apple-signin-client-type-routing.md) — OAuth 흐름과 `*OAuthProperties` 패턴 참고
- 기존 코드
    - [DiscordWebhookAdapter](../../src/main/java/com/umc/product/notification/adapter/out/external/webhook/DiscordWebhookAdapter.java) — Discord 발송 인프라 재사용 후보
    - [ChallengerPart](../../src/main/java/com/umc/product/common/domain/enums/ChallengerPart.java) — 파트 명칭 참고(직접 재사용은 하지 않음)
- Figma 공식 문서
    - [Figma OAuth2](https://www.figma.com/developers/api#oauth2)
    - [GET file comments](https://www.figma.com/developers/api#get-comments-endpoint)
    - [GET file](https://www.figma.com/developers/api#get-files-endpoint)
- Discord 공식 문서
    - [Execute Webhook](https://discord.com/developers/docs/resources/webhook#execute-webhook)
    - [Allowed Mentions](https://discord.com/developers/docs/resources/channel#allowed-mentions-object)
