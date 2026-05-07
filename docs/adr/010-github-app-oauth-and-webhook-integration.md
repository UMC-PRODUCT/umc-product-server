# ADR-004: 단일 GitHub App으로 OAuth 로그인과 Repository Webhook 통합을 동시에 수용한다

## Status

Proposed

## Context

UMC PRODUCT는 Spring/Web/Android/iOS/Node 등 개발 파트 챌린저가 다수 존재하며, 챌린저가 자신의 GitHub repository에서 진행하는 활동(Issue 생성, Pull Request 생성/머지)이 챌린저 운영과 커리큘럼 미션 평가 양쪽에 의미 있는 신호가 된다.

운영 측면에서 다음 두 가지 요구가 동시에 발생했다.

1. **GitHub 계정으로 서비스 로그인**: 개발 파트 챌린저는 대부분 이미 GitHub 계정을 보유하고 있다. 별도 회원가입을 줄이기 위해 Google/Apple/Kakao와 동일한 위치에서 GitHub OAuth 로그인을 추가해야 한다.
2. **본인 repository 활동의 Discord 통지**: 챌린저가 자신의 repository에 Issue를 열거나 PR을 생성/머지하면, 해당 챌린저가 속한 스터디 그룹/프로젝트 Discord 채널로 안내가 전달되기를 원한다. 운영진이 손으로 보지 않아도 활동량을 가시화하고, 추후 커리큘럼 미션 제출의 일부 자동화에도 사용한다.

기술 환경과 제약은 다음과 같다.

- 서비스는 이미 [OAuthProvider](../../src/main/java/com/umc/product/common/domain/enums/OAuthProvider.java) enum과 `member_oauth` 테이블을 통해 Google/Apple/Kakao OAuth 로그인을 운영 중이다. 신규 provider는 enum과 분기 한 곳을 추가하면 흡수 가능한 패턴이 잡혀 있다.
- ADR-003에서 정의한 Discord 발송 인프라(`DiscordWebhookAdapter`, 신설 예정 `SendDiscordMentionPort`)가 존재한다.
- 커리큘럼 도메인(`com.umc.product.curriculum`)에는 `WorkbookMission`, `MissionSubmission`, `ChallengerMission` 등이 있어 GitHub 활동을 미션 신호로 연결할 여지가 있다.
- GitHub은 동일한 목적에 대해 세 가지 인증 메커니즘(OAuth App, GitHub App, Personal Access Token)을 제공한다. 각각 가능한 일과 불가능한 일이 다르고, 두 가지를 함께 운영하면 secret/콜백 URL이 두 배로 늘어 운영 복잡도가 빠르게 증가한다.
- Webhook 수신은 인터넷에 노출되는 엔드포인트가 필요하며, 서명 검증/재전송/중복 방지 책임이 따른다.

이 ADR이 결정해야 하는 사항은 다음과 같다.

- GitHub 인증/통합 방식 (OAuth App vs GitHub App vs PAT, 또는 그 조합)
- Webhook 구독 단위 (개별 repository vs Organization vs App Installation)
- 사용자가 자신의 repository를 등록하는 흐름
- Webhook 수신 후 Discord/Curriculum 측으로 전달하는 책임 경계
- 추후 curriculum 도메인과의 연동 인터페이스

## Decision

우리는 다음과 같이 결정한다.

1. **단일 GitHub App을 발급해 OAuth 로그인과 Webhook 수신을 동시에 처리한다.**
    - "Request user authorization (OAuth) during installation" 옵션을 켜서, GitHub App 자체를 OAuth provider로 사용한다. 별도의 OAuth App을 만들지 않는다.
    - App에는 **user-to-server** 토큰(로그인용)과 **server-to-server** installation token(repository API 호출용) 두 종류의 토큰이 발급된다. 두 토큰의 책임은 명확히 분리한다.

2. **로그인은 user-to-server 토큰의 ID 정보(`/user`, `/user/emails`)만 사용한다.** 발급받은 user access token은 즉시 GitHub user id/login/email을 조회하는 데 사용하고, 서비스 자체 access/refresh token으로 교환한 뒤 메모리에서 폐기한다. user access token을 DB에 영속화하지 않는다.
    - GitHub App의 user-to-server OAuth는 OAuth App과 달리 `authorize` 요청에 `scope` 파라미터를 받지 않는다. 사용자에게 보이는 권한은 GitHub App 설정의 **Account permissions**(예: `Email addresses: Read`)로 결정된다. 토큰 자체는 opaque bearer이므로 JWT 서명 검증 패턴이 아니라 `/user` 호출 결과를 신뢰하는 검증 모델을 사용한다.

3. **Webhook 수신 권한과 repository 접근은 Installation 단위로 관리한다.**
    - 사용자는 GitHub의 "Install this App" 화면에서 본인의 repository(또는 일부)에 App을 설치한다.
    - 설치 시 GitHub이 보내는 `installation` 이벤트로 `installation_id`와 설치된 repository 목록을 수신해 `github_installation`/`github_repo` 테이블에 저장한다.
    - repository API 호출이 필요할 때마다 App private key로 서명한 App JWT로 installation access token을 발급받아 호출한다. installation access token은 1시간 만료이며 절대 영속화하지 않는다.

4. **Webhook 이벤트는 단일 endpoint(`POST /webhooks/github`)에서 수신하고, 서명 검증·이벤트 분기·도메인 이벤트 발행만 담당한다.**
    - HMAC SHA-256 서명(`X-Hub-Signature-256`)을 webhook secret으로 검증한다. 검증 실패 시 401.
    - `X-GitHub-Delivery`(UUID)를 사용해 1차 멱등성을 보장한다(`github_webhook_delivery` 테이블에 INSERT 시 UNIQUE 위반이면 무시).
    - 수신 직후 응답을 200으로 반환하고, 본격 처리는 `ApplicationEventPublisher`로 비동기 위임한다(GitHub은 30초 응답 한도를 가진다).

5. **이슈/PR Discord 알림은 ADR-003의 발송 인프라를 그대로 재사용한다.**
    - 이벤트 핸들러는 `github` 도메인 안에 두고, `notification` 도메인의 `SendWebhookPort`/`SendDiscordMentionPort`를 호출한다.
    - 어느 Discord 채널·role로 보낼지는 `github_repo_link` 테이블의 `discord_channel_webhook_url`/`mention_role_id`에 따른다.

6. **신규 도메인 `com.umc.product.github`을 신설한다.** 외부 시스템 통합은 별도 컨텍스트로 두는 ADR-003의 결정 원칙을 동일하게 적용한다.

7. **커리큘럼 연동은 별도 도메인 이벤트 인터페이스로 분리해 둔다.**
    - 본 ADR 범위에서는 `GithubPullRequestMergedEvent`, `GithubIssueOpenedEvent` 등 도메인 이벤트만 정의하고 발행한다.
    - 커리큘럼 측에서 해당 이벤트를 구독하는 evaluator(예: `MissionPullRequestEvaluator`)는 후속 ADR/티켓에서 다룬다. github 도메인은 curriculum 도메인을 알지 않으며, 그 반대만 허용한다(헥사고날 의존성 방향).
    - 단, curriculum이 이벤트 타입을 import하는 순간 `curriculum → github` 의존이 발생하므로, **이벤트 record 자체는 GitHub-특정 페이로드를 제거한 일반화된 형태로 `com.umc.product.common.event` 또는 별도 `events` 모듈에 둔다.** github 도메인은 webhook payload → 일반 이벤트로 변환만 책임지고, curriculum은 일반 이벤트 타입에만 의존한다.

## Alternatives Considered

### 1. OAuth App + Personal Access Token 조합

GitHub OAuth App으로 로그인을 처리하고, repository 이벤트는 사용자가 발급한 PAT를 서버가 받아 polling하는 방식이다.

장점:

- 가장 단순하다. App JWT/installation token 같은 추가 인증 흐름이 없다.
- OAuth App은 이미 익숙한 표준 OAuth2 흐름이다.

단점:

- PAT는 webhook을 발급할 수 없다. 결국 polling이 강제되며, 알림 지연·rate limit·대량 사용자 확장성 문제가 생긴다.
- 사용자 한 명의 PAT가 그 사람 repository 전체에 접근 가능하므로 권한이 과대 부여된다.
- 사용자가 PAT를 직접 만들고 붙여넣는 UX가 발생한다. 일반 챌린저에게 부담이 크다.

선택하지 않은 이유:
요구사항의 핵심은 "이벤트 기반 Discord 통지"이며, polling은 이 요구를 정면으로 충족하지 못한다. 또한 PAT 입력 UX는 도입 장벽을 키운다.

### 2. OAuth App + 별도의 GitHub App 동시 운영

OAuth App은 로그인 전용으로, GitHub App은 webhook/Installation 전용으로 두 개를 함께 운영하는 방식이다.

장점:

- 책임이 시각적으로 분리된다.
- 로그인용 OAuth client_id가 webhook secret과 섞이지 않는다.

단점:

- client_id/client_secret/private key/webhook secret이 두 세트가 되어 운영 부담이 두 배가 된다.
- 사용자는 "먼저 로그인하고, 그다음 App 설치하기"라는 두 단계 동의 흐름을 거쳐야 하며, 두 흐름의 식별자(login id ↔ installation id)를 백엔드가 직접 잇는 책임이 생긴다.
- GitHub App은 이미 "Request user authorization (OAuth) during installation" 옵션을 통해 OAuth App과 동일한 user-to-server 토큰 발급을 지원하므로, 두 개를 운영해야 할 기술적 필연성이 없다.

선택하지 않은 이유:
운영 secret 수와 동의 화면 단계가 늘어나는 비용이 책임 분리 이득보다 크다. GitHub App 단독으로 두 요구를 동시 충족할 수 있다.

### 3. GitHub Webhook을 Lambda/Cloudflare Worker 같은 별도 인프라에서 수신

서버 본체 대신 별도 함수형 인프라에서 webhook을 수신하고, 우리 서버로 forwarding하는 방식이다.

장점:

- 외부 노출 엔드포인트를 Spring 애플리케이션에서 분리할 수 있다.
- 서명 검증과 재전송을 인프라 레벨에서 흡수할 수 있다.

단점:

- 신규 인프라(IaC, 배포, 모니터링)가 추가된다.
- 서버까지 또 다른 hop이 생기며, 그 구간에 대해서도 인증/멱등성 책임을 새로 정의해야 한다.
- 현 시점에는 webhook 수신량이 많지 않아 분리의 실익이 작다.

선택하지 않은 이유:
처리량이 충분히 커지기 전까지 분리의 이득이 적고, 서명 검증과 비동기 처리는 Spring Boot 어플리케이션 레벨에서 충분히 안전하게 구현할 수 있다. 추후 webhook이 폭증하면 본 ADR을 superseding하는 형태로 재검토한다.

### 4. user access token을 DB에 영속화해 사용자 대신 GitHub API를 호출

사용자가 한 번 로그인한 뒤 그 user access token을 저장해두고, 필요한 GitHub API를 호출할 때 재사용하는 방식이다.

장점:

- 어떤 API든 사용자 권한 그대로 호출할 수 있다.
- App JWT/installation token 흐름을 알지 않아도 된다.

단점:

- user access token을 평문/암호문으로 영속화하는 것 자체가 유출 시 큰 사고로 이어진다(사용자 GitHub 권한 그대로 노출).
- GitHub은 user access token에 대해 default 8시간 만료 + refresh token rotation을 권장하며, refresh 누락이 잦다.
- repository 작업(PR comment 작성, 라벨 부착 등)은 봇 정체성("App")으로 수행하는 편이 audit log·UX 양면에서 명확하다.

선택하지 않은 이유:
보안 노출 면이 너무 크고, GitHub의 권장 패턴(서버는 installation token, 사용자는 user-to-server)과도 어긋난다. 우리는 로그인 직후 user access token을 폐기하고, repository API는 installation token으로만 호출한다.

### 5. Webhook 페이로드를 동기적으로 처리

Spring 컨트롤러에서 webhook을 받자마자 Discord 발송, DB 갱신, 이벤트 발행까지 순차적으로 끝내는 방식이다.

장점:

- 흐름이 단순하다. 비동기 큐/이벤트 인프라가 필요 없다.

단점:

- GitHub의 webhook 응답 SLA(권장 10초, 강제 30초)를 초과할 가능성이 생긴다. Discord 발송 한 곳이 느려지면 GitHub은 webhook을 실패로 간주하고 재전송한다.
- 핸들러 로직 한 부분(예: Discord 발송) 실패가 webhook 전체 실패로 이어진다. 그 결과 같은 이벤트가 반복 재전송된다.

선택하지 않은 이유:
Webhook 수신 컨트롤러는 빠른 200 응답 + 멱등 저장만 책임지고, 처리 자체는 비동기로 분리하는 편이 안정적이다.

## Consequences

### Positive

- GitHub 계정으로 즉시 로그인 가능하며, 동일 App에서 repository 설치까지 자연스럽게 이어진다(연속 동의 화면).
- 모든 GitHub repository API 호출이 봇 ID(App)로 통일되어 audit이 명확하고, 사용자 token 만료에 영향을 받지 않는다.
- Webhook 수신 컨트롤러가 가벼워 GitHub 응답 SLA를 안정적으로 만족한다.
- ADR-003 패턴(외부 통합 도메인 + Discord 발송 인프라 재사용)을 정확히 따라가므로, 이후 다른 외부 시스템 통합에도 일관성이 생긴다.
- 도메인 이벤트(`GithubPullRequestMergedEvent` 등)가 구체적인 외부 시스템(GitHub)에서 분리되어 있어, 커리큘럼 측은 GitHub 자체를 알 필요 없이 구독만 하면 된다.

### Negative

- GitHub App private key, webhook secret, client_id/client_secret이 모두 환경 자산으로 추가되어 secret 관리 항목이 4개 늘어난다.
- App private key 회전(rotation) 절차를 운영 가이드에 별도로 마련해야 한다. 회전 시 모든 인스턴스에서 재배포가 필요하다.
- App 권한(scope)을 변경하면 사용자가 이미 설치한 모든 Installation에서 재승인이 필요하다(GitHub 정책). 권한 설계는 처음부터 보수적으로 잡아야 한다.
- 커리큘럼 연동은 본 ADR에서 인터페이스만 합의하므로, 실제 미션 자동 평가는 별도 작업·별도 ADR로 분리된다. 그 사이의 기간 동안 "GitHub은 받지만 미션엔 영향 없음" 상태가 존재한다.

### Neutral / Trade-offs

- App JWT/installation token 발급 로직은 이번 도입 시점에 한 번 잘 만들어두면 이후 GitHub 관련 모든 기능에 재사용된다. 초기 비용은 있지만 점진적으로 상각된다.
- Webhook 멱등성 저장(`github_webhook_delivery`)은 시간이 지나면 행이 누적된다. 30일 또는 90일 보관 후 삭제하는 정기 잡이 필요하다.
- 로그인 흐름과 설치 흐름을 같은 App에서 처리하므로, "로그인은 했지만 App을 설치하지 않은 사용자"라는 상태가 생긴다. 이 상태는 정상이며, 개발 파트가 아닌 챌린저(예: 디자인/기획)에게도 GitHub 로그인을 제공하기 위해 의도된 분리다.

## Implementation Notes

### 도메인 패키지 구조 (신규)

```
com.umc.product.github/
├── domain/
│   ├── GithubInstallation.java          # installation_id, account_login, app_owner_member_id
│   ├── GithubRepo.java                  # github_repo_id, full_name, installation_id (FK)
│   ├── GithubRepoLink.java              # repo ↔ project/curriculum/discord 채널 매핑
│   ├── GithubWebhookDelivery.java       # X-GitHub-Delivery 멱등성
│   ├── event/
│   │   ├── GithubIssueOpenedEvent.java
│   │   ├── GithubPullRequestOpenedEvent.java
│   │   └── GithubPullRequestMergedEvent.java
│   └── exception/
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   ├── HandleGithubWebhookUseCase.java
│   │   │   ├── RegisterGithubInstallationUseCase.java
│   │   │   ├── LinkGithubRepoUseCase.java
│   │   │   └── GetGithubLoginInfoUseCase.java     # GitHub OAuth 로그인 보조
│   │   └── out/
│   │       ├── LoadGithubInstallationPort.java
│   │       ├── SaveGithubInstallationPort.java
│   │       ├── LoadGithubRepoLinkPort.java
│   │       ├── ExchangeGithubAccessTokenPort.java # OAuth code → user access token
│   │       ├── FetchGithubUserInfoPort.java
│   │       ├── IssueInstallationTokenPort.java    # App JWT → installation token
│   │       └── FetchGithubRepoPort.java
│   └── service/
│       ├── GithubWebhookCommandService.java       # 서명 검증·멱등 저장·이벤트 분기
│       ├── GithubInstallationCommandService.java
│       └── GithubLoginQueryService.java
└── adapter/
    ├── in/
    │   ├── web/                                    # /webhooks/github, /admin/github/*
    │   └── event/                                   # 도메인 이벤트 → Discord 발송 핸들러
    └── out/
        ├── persistence/
        └── external/
            ├── GithubAppProperties.java
            ├── GithubAppJwtFactory.java
            ├── GithubOAuthClient.java               # user-to-server token endpoint
            ├── GithubInstallationClient.java        # installation token, repo API
            └── GithubWebhookSignatureVerifier.java
```

### 데이터 모델 (Flyway 신규)

- `github_installation` (id, installation_id UNIQUE, account_login, account_type, owner_member_id NULLABLE, suspended boolean, installed_at, updated_at)
- `github_repo` (id, github_repo_id UNIQUE, full_name, private_repo boolean, installation_id FK)
- `github_repo_link` (id, github_repo_id FK, link_type ENUM('PROJECT', 'CURRICULUM_WEEK', 'STANDALONE'), target_id BIGINT NULLABLE, discord_channel_webhook_url, mention_role_id NULLABLE, enabled boolean)
    - `link_type`이 `STANDALONE`일 때만 `target_id`가 NULL이어야 한다. CHECK 제약: `(link_type = 'STANDALONE' AND target_id IS NULL) OR (link_type IN ('PROJECT', 'CURRICULUM_WEEK') AND target_id IS NOT NULL)`. 이렇게 분리해야 한 행이 동시에 project와 curriculum 양쪽에 묶이는 모호한 상태를 막는다.
- `github_webhook_delivery` (id, delivery_id UNIQUE, event_type, action, received_at)

### `member_oauth` 확장

- `OAuthProvider` enum에 `GITHUB` 추가.
- 기존 `member_oauth` 테이블 구조는 그대로 사용한다(별도 컬럼 추가 없음). GitHub user id를 `provider_id`로, login을 nickname으로 매핑한다.

### 환경 변수

```yaml
app:
  github:
    app:
      app-id: 123456
      client-id: Iv1.xxxxxxxxxxxx
      client-secret: ${GITHUB_APP_CLIENT_SECRET}
      private-key: ${GITHUB_APP_PRIVATE_KEY}        # PEM, 줄바꿈은 \n으로 인코딩
      webhook-secret: ${GITHUB_APP_WEBHOOK_SECRET}
      setup-url: https://api.umc-product.com/admin/github/installations/setup     # GitHub App "Setup URL"
    oauth:
      authorize-uri: https://github.com/login/oauth/authorize
      token-uri: https://github.com/login/oauth/access_token
      user-info-uri: https://api.github.com/user
      redirect-uri: https://api.umc-product.com/oauth/github/callback              # GitHub App "Callback URL"
      # GitHub App은 OAuth App과 달리 authorize 요청에 scope 파라미터를 사용하지 않는다.
      # 사용자에게 노출되는 권한은 GitHub App 설정의 Account permissions로 결정된다
      # (최소: "Email addresses: Read").
```

GitHub App 측 설정 페이지에서는 다음 두 URL을 별도로 등록한다.

- **Setup URL**: 사용자가 App 설치를 완료한 직후 이동하는 URL. `installation_id`, `setup_action` 쿼리 파라미터를 받는다.
- **Callback URL**: OAuth user-to-server 토큰 교환을 위한 redirect URI. `code`, `state` 파라미터를 받는다.

### Webhook 처리 흐름

1. `POST /webhooks/github`을 컨트롤러에서 `@RequestBody byte[] rawBody`로 수신한다. Jackson이 InputStream을 소비하기 전에 원본 바이트를 보존해야 HMAC 검증이 깨지지 않는다(JSON 재직렬화 결과는 공백·키 순서가 달라 1바이트 차이로 서명 불일치 발생).
2. 컨트롤러는 `@Transactional`을 붙이지 않는다. `X-Hub-Signature-256` 헤더의 `sha256=…` 부분과 webhook secret으로 HMAC SHA-256을 계산해 `MessageDigest.isEqual`로 상수 시간 비교한다. 실패 시 401, 본문은 로깅하지 않는다.
3. 검증 통과 후, **별도의 짧은 트랜잭션**(`Propagation.REQUIRES_NEW`)에서 `github_webhook_delivery(delivery_id)` UNIQUE INSERT를 시도한다. 이 트랜잭션은 후속 처리와 분리되어야 한다 — INSERT만 먼저 commit되어야 장애·재기동 후의 GitHub 재전송이 같은 `delivery_id`로 들어왔을 때 멱등 처리가 보장된다. UNIQUE 위반이면 200으로 즉시 종료.
4. INSERT 성공 후 `X-GitHub-Event` + `payload.action`을 도메인 이벤트로 변환해 `ApplicationEventPublisher.publishEvent(...)`. PR "merged"는 별도 이벤트가 아니라 `pull_request` 이벤트의 `action == "closed" && pull_request.merged == true` 조합으로만 도출된다(이벤트 변환기에서 분기).
5. 컨트롤러는 200 OK를 즉시 반환한다(GitHub의 30초 SLA 안에 응답).
6. `@TransactionalEventListener(phase = AFTER_COMMIT) + @Async("githubWebhookExecutor")` 핸들러가 Discord 발송 등 후속 작업을 수행한다.
7. `@EnableAsync`와 전용 `ThreadPoolTaskExecutor`(예: corePoolSize 4, maxPoolSize 16, queueCapacity 200, RejectedExecutionHandler `CallerRunsPolicy`)를 명시한다. Spring 기본 SimpleAsyncTaskExecutor는 매 호출 새 스레드를 만들어 운영 환경에서 위험하다.
8. Spring Security 설정에서 `/webhooks/github` 경로는 `permitAll`로 열되, **인증 우회는 컨트롤러 진입 직후의 서명 검증으로만 통제**한다. CORS는 차단(브라우저 호출 대상이 아님).

### App JWT / Installation Token 발급

- App JWT: 헤더 `RS256`, 페이로드 `iat`(현재-60s), `exp`(현재+9m), `iss=app_id`. 서명은 App private key.
- Installation token: `POST /app/installations/{installation_id}/access_tokens` (Authorization: `Bearer {appJwt}`). 응답의 `token`은 1시간 유효, **메모리 캐시(예: Caffeine)에 만료 5분 전까지만 보관**. DB 영속화 금지.

### Discord 발송 라우팅

- 수신한 webhook payload의 `repository.id`(GitHub 측 식별자) → `github_repo.github_repo_id` → `github_repo_link.github_repo_id` 순으로 조회한다.
- `discord_channel_webhook_url`로 [DiscordWebhookAdapter](../../src/main/java/com/umc/product/notification/adapter/out/external/webhook/DiscordWebhookAdapter.java) 또는 ADR-003에서 정의한 `SendDiscordMentionPort`를 호출.
- 매핑이 없으면 발송 생략(에러 아님). `enabled = false`인 링크도 발송 생략.

### 멀티 인스턴스 운영 시 한계

`ApplicationEventPublisher`는 in-process 이벤트 버스다. webhook을 수신한 인스턴스에서만 핸들러가 동작한다.

- **Discord 발송**은 webhook 수신 인스턴스에서 그대로 처리되므로 영향 없음.
- **추후 curriculum 측 평가 작업**이 이벤트를 다른 인스턴스에서 받을 필요가 생기면, `Spring ApplicationEvent` 대신 외부 메시지 브로커(Redis Streams, Kafka 등)로 격상해야 한다. 본 ADR은 in-process로 한정하고, 분산 처리가 실제 요구가 될 때 superseding ADR로 전환한다.

### 보안 주의사항

- App private key는 KMS 또는 Spring `Jasypt` 대칭 암호화 후 환경변수로 주입한다. 평문 .env 커밋 금지.
- webhook endpoint는 인증 필터에서 화이트리스트로 두되, 모든 요청을 서명 검증으로만 통과시킨다. CORS는 차단(브라우저에서 호출되지 않는 endpoint).
- user access token은 GitHub API 호출 직후 폐기. 로그/응답 본문 어디에도 출력 금지.

## Implementation Plan (Commit 단위)

각 커밋은 독립적으로 빌드/테스트가 통과해야 하며, 메시지는 Conventional Commits 규칙(`<type>: <subject>`)을 따른다. 묶음 PR 제목은 `[Feat] GitHub App OAuth 로그인 및 Webhook 통합 도입` 형태로 한다.

### Phase 1 — GitHub OAuth 로그인 (사용자 인증)

1. `chore: github 도메인 패키지와 GithubAppProperties 스켈레톤 추가`
    - `com.umc.product.github` 하위 빈 패키지 + `GithubAppProperties`(record) + `application*.yml`에 `app.github.*` 키 추가.
    - **Setup URL**(`app.github.app.setup-url`)과 **OAuth Callback URL**(`app.github.oauth.redirect-uri`)을 분리 등록한다. `scope` 키는 두지 않는다(GitHub App은 사용 안 함).
    - 외부 호출 없음. 빈 컨트롤러/서비스 인터페이스만 생성.

2. `feat: OAuthProvider enum에 GITHUB 추가`
    - `OAuthProvider.from`에 `"github"` 케이스 추가.
    - GitHub 토큰은 JWT가 아니므로 기존 `OAuthTokenVerificationAdapter` 분기에 직접 끼우지 않는다. GitHub용 검증은 별도 어댑터(`GithubUserInfoClient`)로 분리해 다음 커밋에서 도입한다. 본 커밋에서 분기에 placeholder 코드는 넣지 않는다(컴파일 가능한 dead code 방지).

3. `feat: GitHub OAuth user-to-server 토큰 교환 어댑터 구현`
    - `GithubOAuthClient`: `code` → `access_token` 교환(`Accept: application/json` 명시). 요청 시 `scope` 파라미터를 보내지 않는다.
    - `GithubUserInfoClient`: `/user`, `/user/emails`로 GitHub user id/login/primary email 조회.
    - 단위 테스트는 `MockRestServiceServer`로 200/4xx 케이스 검증, `403`(권한 부족) 분기 포함.

4. `feat: GitHub OAuth 로그인 유즈케이스를 기존 OAuth 로그인 흐름에 연결`
    - `OAuthLoginCommandService`(또는 동등 위치)에서 GITHUB 분기를 추가해, code → user info → `member_oauth` 조회/생성 → 서비스 access/refresh token 발급. user access token은 메서드 종료 시 폐기.
    - 테스트: 신규 가입과 기존 회원 로그인 두 시나리오 모두 한국어 `@DisplayName`.

### Phase 2 — GitHub App Installation 수용

5. `feat: github 통합 테이블 4종 Flyway 마이그레이션 작성`
    - `github_installation`, `github_repo`, `github_repo_link`, `github_webhook_delivery` 생성.
    - `github_repo_link`는 `link_type ENUM('PROJECT', 'CURRICULUM_WEEK', 'STANDALONE') + target_id BIGINT` 구조 + CHECK 제약(STANDALONE일 때만 target_id NULL).
    - 인덱스: `github_repo(installation_id)`, `github_repo_link(github_repo_id)`, `github_webhook_delivery(received_at)`.

6. `feat: GithubInstallation/GithubRepo 도메인 및 Persistence Adapter 구현`
    - 엔티티/Builder/도메인 메서드(`suspend`, `unsuspend`, `assignOwner`).
    - JPA Repository와 Persistence Adapter, Save/Load Port.
    - Repository 메서드는 CLAUDE.md의 read 메서드 시맨틱 규칙을 따름(`get` vs `find` vs `list`).

7. `feat: GitHub App JWT 생성 및 installation access token 발급기 구현`
    - `GithubAppJwtFactory`: RS256 서명, `iat`(now-60s)/`exp`(now+9m, GitHub 한도 10m 미만)/`iss`(app_id) 클레임.
    - `IssueInstallationTokenPort` + `GithubInstallationClient`: token 발급 + 만료 5분 전 폐기 기준의 `Caffeine` 캐시. **DB 영속화 금지**.
    - 단위 테스트: 만료 직전 캐시 hit, 만료 후 재발급, 401 응답 시 캐시 invalidate.

8. `feat: GitHub App Setup URL 콜백과 OAuth Callback 분리 구현`
    - `GET /admin/github/installations/setup`: 설치 직후 `installation_id`, `setup_action`을 받아 Installation 레코드를 활성화/비활성화. (Setup URL)
    - `GET /oauth/github/callback`: OAuth user-to-server 토큰 교환 진입점. (Callback URL, 커밋 4와 연결)
    - **두 endpoint를 하나의 컨트롤러에 합치지 않는다.** 두 흐름은 GitHub 측 등록 URL이 다르고 책임도 다르다.

9. `feat: 운영진용 repo 링크 관리 admin API 추가`
    - `POST /admin/github/repos/{repoId}/links`: 운영진이 repo ↔ (프로젝트 또는 커리큘럼 주차 또는 standalone) ↔ Discord 채널 매핑을 등록.
    - 모든 admin endpoint는 admin role 가드 적용 + 입력 DTO record + `@Valid`. `link_type`/`target_id` 정합성은 record `compact constructor`에서 검증.

### Phase 3 — Webhook 수신과 Discord 통지

10. `chore: webhook 보안 인프라 구성`
    - Spring Security `SecurityFilterChain`에 `/webhooks/github` `permitAll` 규칙 추가(JWT/세션 검증 우회).
    - `@EnableAsync` 활성화 + 전용 `ThreadPoolTaskExecutor("githubWebhookExecutor")` 빈 정의(corePoolSize 4, maxPoolSize 16, queueCapacity 200, `CallerRunsPolicy`).
    - 본 커밋은 인프라만 추가, 실제 webhook 컨트롤러는 다음 커밋에서.

11. `feat: GitHub webhook 서명 검증 및 멱등성 저장기 구현`
    - `GithubWebhookSignatureVerifier`: webhook secret + raw body로 HMAC-SHA256 계산, `MessageDigest.isEqual` 상수 시간 비교.
    - `GithubWebhookDeliveryService.recordIfAbsent(deliveryId)`: `Propagation.REQUIRES_NEW` 트랜잭션 내에서 UNIQUE INSERT 시도 후 즉시 commit. UNIQUE 위반은 false 반환.
    - 단위 테스트는 동일 deliveryId 재호출 시 false, 다른 deliveryId는 true 반환을 검증.

12. `feat: POST /webhooks/github 수신 컨트롤러와 도메인 이벤트 발행 구현`
    - 컨트롤러는 `@RequestBody byte[] rawBody`로 수신, `@Transactional` 없이 동작. 서명 검증 → `recordIfAbsent` → 통과 시 도메인 이벤트 발행 → 200 OK.
    - `GithubWebhookEventConverter`가 `X-GitHub-Event` + `payload.action` 분기로 도메인 이벤트(`GithubIssueOpenedEvent`, `GithubPullRequestOpenedEvent`, `GithubPullRequestMergedEvent`) 생성.
    - **PR merged는 `pull_request` + `action="closed"` + `pull_request.merged=true` 조합**으로만 도출(별도 webhook 이벤트 아님).
    - `installation`, `installation_repositories` 이벤트는 `GithubInstallation`/`GithubRepo` 상태 동기화에만 사용(Discord 발송 X).

13. `feat: github 이벤트 → Discord 통지 핸들러 구현`
    - `@TransactionalEventListener(phase = AFTER_COMMIT) + @Async("githubWebhookExecutor")` 핸들러에서 `github_repo_link` 조회(repository.id → github_repo → link) → Discord embed 생성 → ADR-003의 발송 포트 호출.
    - embed 포맷: `[{repo}] PR opened: {title}\nby {actor}\n{html_url}` 등 이벤트별 한국어 라벨.
    - 통신 실패 시 `last_error` 기록, 자체 재시도 금지(GitHub 재전송과 멱등성 표가 이미 보호 중).

14. `chore: 오래된 webhook delivery 정리 스케줄러 추가`
    - `@Scheduled(cron = "0 0 4 * * *")`로 90일 이상 지난 `github_webhook_delivery`를 batch DELETE. ShedLock(또는 동등 인프라)로 멀티 인스턴스에서 한 번만 실행되게 보장.

### Phase 4 — 커리큘럼 연동 인터페이스 (후속 작업의 출발점)

15. `refactor: github 도메인 이벤트를 공용 event 패키지로 이전`
    - `com.umc.product.common.event` 또는 별도 `events` 모듈에 `GithubIssueOpenedEvent`, `GithubPullRequestOpenedEvent`, `GithubPullRequestMergedEvent` record 이전.
    - 페이로드는 GitHub-특정 raw payload가 아닌 일반화된 식별자(`fullName`, `prNumber`, `actorLogin`, `mergedAt` 등)만 포함.
    - **github 도메인은 webhook payload → 일반 이벤트 변환만 책임지고, curriculum 도메인은 일반 이벤트 타입에만 의존한다.** 이렇게 두면 `curriculum → github` 직접 의존을 피할 수 있다.

16. `docs: github 통합 운영 가이드 작성 (선택)`
    - `docs/guides/github-app-integration.md`: App 생성/권한 설정, Setup URL과 Callback URL 등록, 설치 흐름, repo 매핑 추가, webhook secret/private key 회전 절차, 장애 대응 체크리스트.
    - 본 ADR과 상호 링크.

> 커리큘럼 측의 실제 평가 로직(예: PR merge → 미션 완료 처리)은 본 ADR이 정의한 도메인 이벤트를 구독하는 별도 PR로 이어가며, 그 결정은 별도 ADR(예: ADR-005)에서 다룬다.

## References

- 관련 ADR
    - [ADR-001: Apple 로그인 ClientType 라우팅](001-apple-signin-client-type-routing.md) — `*OAuthProperties` 패턴, `OAuthProvider` 분기 패턴 참조
    - [ADR-003: Figma 댓글 Discord 포워딩](003-figma-comment-discord-forwarder.md) — 외부 시스템 통합 도메인 분리 원칙, Discord 발송 인프라 재사용 모델
- 기존 코드
    - [OAuthProvider](../../src/main/java/com/umc/product/common/domain/enums/OAuthProvider.java) — provider enum
    - [DiscordWebhookAdapter](../../src/main/java/com/umc/product/notification/adapter/out/external/webhook/DiscordWebhookAdapter.java) — Discord 발송 인프라
    - [Curriculum 도메인](../../src/main/java/com/umc/product/curriculum) — 향후 GitHub 이벤트를 구독할 후보
- GitHub 공식 문서
    - [About creating GitHub Apps](https://docs.github.com/en/apps/creating-github-apps/about-creating-github-apps)
    - [Authenticating as a GitHub App](https://docs.github.com/en/apps/creating-github-apps/authenticating-with-a-github-app/authenticating-as-a-github-app-installation)
    - [Generating a user access token for a GitHub App](https://docs.github.com/en/apps/creating-github-apps/authenticating-with-a-github-app/generating-a-user-access-token-for-a-github-app)
    - [Webhook events: pull_request](https://docs.github.com/en/webhooks/webhook-events-and-payloads#pull_request)
    - [Webhook events: issues](https://docs.github.com/en/webhooks/webhook-events-and-payloads#issues)
    - [Validating webhook deliveries](https://docs.github.com/en/webhooks/using-webhooks/validating-webhook-deliveries)
