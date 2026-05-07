# Figma 댓글 dev / prod 중복 발송 방지 계획

> 작성일: 2026-05-08
> 대상: dev / prod 두 환경에서 동일한 figma watched_file 들을 동시에 폴링할 때 같은 댓글이 Discord 채널에 두 번 발송되는 사고를 막기 위한 다층 방어 설계.
> 목적: 단일 핫픽스가 아니라 **무엇이 잘못되어도 중복 발송이 안 되도록** 하는 4 단계 안전망 + 4 commit 실행계획.

## 0. TL;DR

dev / prod 가 **각자 별도 DB 를 갖되 같은 figma file_key 들을 폴링**하면, 양쪽 모두 자기만의 `figma_comment_dispatch` UNIQUE 가드만 보고 있어 cross-env dedup 메커니즘이 0 이다. 한 댓글이 양쪽에서 "처음 본 댓글" 로 처리되어 LLM 분류 + Discord 발송이 두 번 일어난다.

해결: **boring by default, defense in depth.** 한 가지에 의존하지 않고 4 단계 안전망을 동시에 둔다.

| 층 | 메커니즘 | 어디서 | 효과 |
|----|----------|--------|------|
| **L1** | `app.figma.sync.enabled` 환경변수 (이미 존재) | yaml | dev 는 기본 false → 폴링 자체가 안 일어남 |
| **L2** | `figma_routing_domain.discord_webhook_url` 을 환경별 다른 채널로 등록 | DB 운영 정책 | 만약 발송되더라도 채널이 격리되어 prod 채널 오염 0 |
| **L3** | 신규 `app.figma.sync.discord-enabled` 토글 | yaml | sync 흐름은 돌리되 Discord 발송만 차단 (dev/staging dryRun 모드) |
| **L4** | embed footer 에 `[ENV: dev]` 환경 라벨 강제 표시 | 어댑터 | 잘못된 채널로 가더라도 사고를 즉시 인지 |

L1 / L4 가 실수해도 L2 / L3 가 막고, L2 / L3 가 실수해도 L1 / L4 가 잡는 구조. 4 commit 으로 마무리.

## 1. 문제 정밀 진단

### 1.1 왜 dispatch UNIQUE 가드가 cross-env dedup 을 못 막는가

[`figma_comment_dispatch.comment_id`](../../src/main/resources/db/migration/V2026.05.07.10.00__create_figma_tables.sql) 의 UNIQUE 제약은 **단일 PostgreSQL 인스턴스 scope**.

```
┌─────────────── prod ───────────────┐    ┌─────────────── dev ────────────────┐
│ DB: umc_product_prod                │    │ DB: umc_product_dev                 │
│ figma_comment_dispatch              │    │ figma_comment_dispatch              │
│   - comment_id = "C1" (UNIQUE)      │    │   - (비어 있음. C1 이 여기엔 없음.)  │
│   - dispatched_at = T1              │    │                                     │
│ figma_summary_cursor                │    │ figma_summary_cursor                │
│   - last_window_end = T1            │    │   - last_window_end = T0            │
└─────────────────────────────────────┘    └─────────────────────────────────────┘
                ▲                                          ▲
                │ poll                                     │ poll
                └────────── 같은 figma_watched_file 행 ────┘
                                  ▲
                          ┌───────┴───────┐
                          │ Figma Cloud   │
                          │ comment "C1"  │
                          └───────────────┘
```

→ 양쪽 인스턴스 모두 "C1 은 dispatch 에 없으니 신규 댓글" 로 본다 → 양쪽 모두 LLM 분류 → 양쪽 모두 같은 Discord webhook URL 로 POST → **한 댓글이 채널에 2회 노출**.

### 1.2 도입 가능한 cross-env dedup 메커니즘과 비용

| 메커니즘 | 효과 | 비용 |
|----------|------|------|
| 두 환경이 같은 DB 공유 | dispatch UNIQUE 가 자동 작동 | cursor 한 row 를 두 인스턴스가 동시 advance → race. cursor 에 강한 락 + ShedLock 수준 인프라 필요. dev 가 prod DB 를 오염시킬 위험 매우 큼. **반대로 풀어야 할 안티패턴.** |
| 외부 분산 cache (Redis SETNX) | 환경 무관 dedup | Redis 신규 인프라 + dev / prod 가 같은 Redis 를 봐야 함. 인프라 결합도 증가. |
| Figma webhook + 단일 수신자 | dev 는 webhook 미수신 | Figma Enterprise plan 필요 (ADR-003 §Alternatives 2 에서 이미 기각). |
| **dev 가 발송하지 않게 (환경 격리)** | 가장 단순. 인프라 변화 0. | dev 에서 Discord 발송 검증을 "별도 dev webhook" 또는 "preview API 만" 으로 우회. |

운영 합리성 (boring by default): **마지막 옵션 (환경 격리)** 가 최소 비용 + 최대 안전. 단, "혹시" 에 대비해 다층으로 둔다.

## 2. 평가한 5 옵션

### A — `FIGMA_SYNC_ENABLED=false` 만 dev 에서 강제 (현재 기본값) ✅ 채택 (L1)

[`FigmaCommentSyncScheduler.poll`](../../src/main/java/com/umc/product/figma/adapter/in/scheduler/FigmaCommentSyncScheduler.java) 가 `figmaSyncProperties.enabled()` 를 첫 줄에서 검사. false 면 즉시 return.

장점:
- 이미 구현되어 있음. 추가 코드 0.
- prod 만 enabled 켜면 dev 폴링 자체가 정지 → 문제의 원천 차단.

단점:
- dev 에서 figma flow (분류 / 발송) 의 통합 검증을 하려면 일시적으로 enabled 켜야 함. 그 순간 prod 와의 dedup 이 깨짐.
- "혹시 운영자가 dev .env 에서 enabled 켜둠" 같은 휴먼 에러에 대한 백업 가드가 없음.

선택 이유:
1차 안전망. 단독으로 충분하지 않으니 L2~L4 와 같이 둔다.

### B — 환경별 Discord webhook URL 분리 (자연 격리) ✅ 채택 (L2)

`figma_routing_domain.discord_webhook_url` 를 dev DB 에는 dev 전용 테스트 채널로, prod DB 에는 운영 채널로 등록. 데이터 격리 → 발송이 가도 채널이 다름 → prod 채널 오염 0.

장점:
- 인프라 / 코드 변경 0. 운영자가 dev DB 에 admin API 로 등록할 때 webhook URL 만 다르게 두면 끝.
- L1 이 실수로 켜져 있어도 잘못된 채널로 가지 않음.

단점:
- 운영자 부담. dev / prod 의 routing_domain 행을 따로 관리해야 함.
- 운영자가 실수로 dev DB 에 prod webhook URL 을 등록하면 가드 무력화. → L4 환경 라벨이 그래서 필요.

선택 이유:
2차 안전망. L1 이 무력화돼도 채널 격리로 사고 영향 0.

### C — sync 는 돌리되 Discord 발송만 차단 (dryRun 모드) ✅ 채택 (L3)

신규 `app.figma.sync.discord-enabled` 토글. false 면 [`FigmaCommentSummaryService`](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentSummaryService.java) 가 발송만 건너뛰고 dispatch / cursor 갱신은 그대로 수행. dev / staging 에서 **분류 정확도 + 캐시 동작 + cursor 진행** 까지 검증하면서 Discord 만 건드리지 않음.

장점:
- L1 (FIGMA_SYNC_ENABLED) 보다 더 정밀한 검증 환경. dev 에서도 figma → LLM 분류 → cache 적재까지 실제로 돌리면서 발송만 차단.
- preview API 와 비슷한 시맨틱이지만 스케줄러 자동 실행 path 도 보호.

단점:
- 신규 토글 1 개 추가. 코드 변경 작음 (FigmaCommentSummaryService 의 `command.dryRun()` 분기 옆에 OR 조건 추가).
- 운영자가 prod 에 실수로 false 를 두면 발송이 멈춤. → 모니터링 필요 (gauge 노출).

선택 이유:
3차 안전망. dev 에서 figma 검증을 "발송만 빼고" 안전하게 돌리고 싶을 때 사용. L1 의 "발송 자체를 안 하니 검증도 안 됨" 한계를 보완.

### D — embed footer 에 환경 라벨 강제 표시 ✅ 채택 (L4)

[`DiscordMentionWebhookAdapter.buildFooter`](../../src/main/java/com/umc/product/figma/adapter/out/external/DiscordMentionWebhookAdapter.java) 가 footer 에 `[ENV: prod]` / `[ENV: dev]` / `[ENV: staging]` 을 강제로 prefix.

장점:
- 잘못된 채널로 가더라도 footer 만 보면 즉시 환경 식별 가능 → 사고를 5분 안에 인지.
- 코드 변경 매우 작음 (footer 문자열 하나 + properties 1 개).

단점:
- 사고를 막지는 못하고 **인지 시간만 단축**. 다른 안전망과 조합해서만 의미.

선택 이유:
4차 안전망. L1~L3 가 모두 실패해도 footer 라벨로 사고 인지 가능.

### E — 같은 DB 공유 + 분산 락 / Redis dedup ❌ 기각

dev / prod 가 같은 DB 또는 Redis 를 보면 cross-env dedup 이 자동 적용. 하지만:

- dev 가 prod DB / Redis 를 오염시킬 가능성 (운영 사고 시 데이터 손상 영향이 양쪽으로 번짐).
- cursor / dispatch 의 race 가 새로 발생 → ShedLock 도입 필요.
- 인프라 결합도 증가. dev 가 prod 인프라에 접근해야 한다는 불편한 보안 가정.

선택 안 함:
boring by default 원칙 위반. 인프라 결합도를 높이는 대가가 cross-env dedup 의 가치보다 훨씬 큼. dev / prod 환경 격리는 일반 운영 best practice.

## 3. 권장 결정 — 다층 방어 (Defense in Depth)

```
        Figma Cloud (comment "C1")
                  │
                  ▼
    ┌─────────────────────────────────┐
    │ L1: app.figma.sync.enabled      │  ← dev 기본 false. 이미 구현됨.
    │     false면 폴링 자체 건너뜀     │
    └─────────────────┬───────────────┘
                      │ enabled=true
                      ▼
    ┌─────────────────────────────────┐
    │ L3: app.figma.sync              │  ← 신규 토글. dev/staging 기본 false.
    │     .discord-enabled=false       │     발송만 차단, 분류/캐시는 돌림.
    │     이면 발송 단계 skip          │
    └─────────────────┬───────────────┘
                      │ discord-enabled=true
                      ▼
    ┌─────────────────────────────────┐
    │ L2: figma_routing_domain         │  ← 운영 데이터 정책.
    │     .discord_webhook_url         │     dev DB 는 dev 채널, prod DB 는 운영 채널.
    │     (환경별로 다른 채널)          │
    └─────────────────┬───────────────┘
                      │
                      ▼
    ┌─────────────────────────────────┐
    │ L4: embed footer 에              │  ← 신규 코드.
    │     [ENV: dev/prod] 강제 표시     │     사고 인지 시간 단축.
    └─────────────────┬───────────────┘
                      │
                      ▼
              Discord 채널 도달
```

**핵심 정합성 규칙:**
- 운영 환경 (`SPRING_PROFILES_ACTIVE=prod`) 에서만 L1 + L3 둘 다 true.
- dev / staging 에서는 L1 또는 L3 중 적어도 하나 false 가 yaml profile 기본값.
- L2 의 webhook URL 은 운영 데이터라 코드/yaml 가 아닌 admin API 로 등록되며, 본 보고서는 운영 가이드 문구로 보강.
- L4 는 환경 라벨이 yaml properties (`app.environment` = `${spring.profiles.active}`) 에서 자동 주입.

## 4. Commit 단위 실행계획

각 commit 은 단독 빌드/테스트 통과해야 하며, Conventional Commits 규칙을 따른다. PR 1 건으로 묶어 `[Feat] Figma sync 환경 격리 다층 방어` 로 머지.

### Commit 1 — `feat: app.environment property 와 figma sync.discord-enabled 토글 추가`

- `application.yml` 에 `app.environment: ${spring.profiles.active}` 추가 (root 레벨 또는 `app:` 블록 안). 이미 `springdoc.environment` 에 같은 값이 있으므로 패턴 통일.
- `FigmaSyncProperties` 에 `discordEnabled` 필드 추가, default `true` (기존 동작 보존).
- `application.yml` 에 `app.figma.sync.discord-enabled: ${FIGMA_SYNC_DISCORD_ENABLED:true}` 추가.
- 이 commit 시점에는 어떤 코드도 새 필드를 사용하지 않음 → 동작 변경 0.
- 영향 파일: `application.yml`, `FigmaSyncProperties.java`.

### Commit 2 — `feat: figma sync 가 discord-enabled=false 면 발송 단계만 skip (L3)`

- `FigmaCommentSummaryService.summarize` 의 발송 분기에 properties.discordEnabled() 체크 추가:
  ```java
  if (!command.dryRun()
      && !sendable.isEmpty()
      && figmaSyncProperties.discordEnabled()) {
      sent = sendDomainBatch(...);
      // dispatch insert + cursor advance 는 sent=true 일 때만 그대로 일어남.
  }
  ```
- `discord-enabled=false` 일 때:
  - sent=false 로 응답 (dryRun 과 비슷한 효과).
  - dispatch insert / cursor advance 도 건너뜀 (false sent 상태로 dispatch 행을 만들면 안 됨 — 다음 사이클에 발송 안 한 댓글이 dispatch 가드에 막혀 영원히 발송 안 될 위험).
  - markIdle / 분류 결과 L1·L2 캐시 적재는 그대로 → 분류 정확도 검증 가능.
- 단위 테스트 1 건 추가: `discord_disabled_시_분류_캐시는_적재되지만_발송_dispatch_cursor_미변경`.
- 영향 파일: `FigmaCommentSummaryService.java`, `FigmaCommentSummaryServiceTest.java`.

### Commit 3 — `feat: Discord embed footer 에 환경 라벨 강제 표시 (L4)`

- `DiscordMentionWebhookAdapter` 에 `app.environment` 주입.
- `buildFooterText` 의 결과 prefix 에 `[ENV: dev|staging|prod]` 추가:
  ```
  [ENV: dev] Figma · 2026-05-08 12:34 ~ 2026-05-08 12:39 KST
  ```
- prod 환경에서도 라벨이 항상 노출되어 운영자가 채널의 환경 출처를 즉시 식별.
- 단위 테스트: footer 문자열에 환경 라벨이 prefix 됐는지 검증.
- 영향 파일: `DiscordMentionWebhookAdapter.java`, properties 주입 관련 config 1 곳, 테스트.

### Commit 4 — `docs: 환경별 figma sync 운영 가이드 + dev/prod 격리 정책 추가`

- `docs/guides/Figma_댓글_Discord_포워딩_보고서.md` 상단 amendment 영역에 다음 운영 정책 추가:
  - **L1**: prod profile 만 `FIGMA_SYNC_ENABLED=true`. dev / staging / local 은 모두 false 기본.
  - **L2**: dev DB 의 `figma_routing_domain.discord_webhook_url` 은 반드시 dev 전용 테스트 채널. 같은 운영 채널 webhook 을 dev DB 에 등록하지 않는다.
  - **L3**: prod 만 `FIGMA_SYNC_DISCORD_ENABLED=true`. dev 에서 분류 / 캐시 검증을 돌리고 싶을 때만 임시로 true (위 L2 가 방어).
  - **L4**: 어떤 환경의 메시지든 footer 의 `[ENV: ...]` 라벨로 즉시 식별 가능.
  - 운영자 체크리스트: 신규 환경 부팅 시 4 단계 모두 확인하는 절차.
- `docs/adr/004-figma-comment-time-window-unification.md` 의 §Implementation Notes › 운영 시 주의사항에 한 줄 추가 ("환경 격리는 §본 가이드 참조").
- 영향 파일: 운영 가이드, ADR-004 짧은 amendment.

## 5. 위험 / 트레이드오프

| 항목 | 영향 | 완화책 |
|------|------|--------|
| Commit 2 의 `discordEnabled=false` 가 prod 에 실수로 배포되면 모든 댓글이 발송 안 됨 (silent failure) | 운영자가 사고 인지 못 함 | `app.figma.sync.discord-enabled=false` 일 때 L4 와 별도로 `llm.active.provider.info` 같은 패턴으로 gauge metric (`figma.sync.discord_enabled` = 0/1) 추가 노출. 본 commit 4 에 포함하거나 후속 PR 로 분리. |
| L2 의 dev / prod webhook URL 분리는 운영자 휴먼 에러에 의존 | 운영자가 dev DB 에 prod webhook 을 등록하면 가드 무력화 | L4 의 footer 라벨로 사고 인지 시간 단축. 그리고 운영 가이드 (commit 4) 에 admin API 등록 시 환경 라벨 명시. |
| `app.environment` 값이 `prod` / `dev` 외 임의 string 일 수 있음 | footer 라벨이 의도치 않은 값 (예: `local`, `test`) 으로 표시 | 임의 값이라도 운영자가 환경을 식별할 수 있으면 충분. 강제 enum 으로 좁히면 신규 profile 추가 시 footer 가 깨질 위험. 자유 string 으로 둠. |
| 다중 인스턴스 prod 가 같은 DB 를 보는 경우 (정상) vs dev 와 prod 가 같은 DB 를 보는 경우 (사고) | 후자는 본 계획의 범위 밖이지만 발생 가능 | 운영 가이드에 "dev 와 prod 는 절대 같은 DB / 같은 Discord webhook URL 을 공유하지 않는다" 를 inviolable rule 로 명시. |
| 본 다층 방어는 "동일 figma file_key 를 dev / prod 가 모두 폴링" 가정 | 만약 dev 가 별도 figma 파일을 본다면 본 계획이 과해 보임 | 그래도 L4 footer 라벨은 운영 일반에 가치 있고 (사고 인지), L1 은 이미 default 이므로 비용이 거의 0. |

## 6. NOT in scope

본 계획에서 의도적으로 다루지 않는 항목.

- **Figma webhook 도입**: Enterprise plan 필요 (ADR-003 §Alternatives 2 에서 기각). 본 계획은 polling 구조를 유지한 채 환경 격리만 다룬다.
- **dev / prod DB 공유 또는 cross-env Redis dedup**: 인프라 결합도 증가, 운영 사고 영향 확대 (옵션 E 에서 기각). 환경 분리 원칙을 깨뜨리는 방향.
- **분산 락 / ShedLock**: 다중 인스턴스 격리 문제는 별개 (ADR-004 §Negative 에서 후속 작업으로 분리됨). 본 계획은 환경(dev/prod) 격리에 한정.
- **운영 자동화 (admin endpoint 으로 환경별 webhook URL 일괄 import / export)**: 운영자 부담은 인정하지만 별도 ADR 의 admin endpoint 결정과 묶어 다룬다.
- **알림 인지 SLO 정의**: footer 라벨로 사고 인지 시간을 단축하지만 SLO 수립 / 페이저 / on-call 정책은 운영팀 영역.

## 7. 검증 시나리오 (PR 머지 전 smoke test)

### 7.1 prod 단독 환경 (dev 가 꺼져 있는 정상 상태)

1. `SPRING_PROFILES_ACTIVE=prod`, `FIGMA_SYNC_ENABLED=true`, `FIGMA_SYNC_DISCORD_ENABLED=true` 로 부팅.
2. 댓글 1 건이 figma 에 추가됨.
3. 다음 사이클에서 prod 가 분류 + 발송 → 운영 채널에 1 회 노출.
4. embed footer 에 `[ENV: prod] Figma · ...` 표시 확인.
5. `figma_comment_dispatch` 에 commentId 행 1 개 추가 확인.
6. 다음 사이클에 같은 댓글이 다시 들어와도 dispatch dedup 으로 발송 0 회 확인.

### 7.2 dev 도 같이 부팅한 환경 (사고 상황 시뮬레이션)

1. dev 인스턴스에서 `FIGMA_SYNC_ENABLED=true` 를 의도적으로 켜서 L1 우회 (L1 만 끄지 않은 상태).
2. dev DB 의 routing_domain webhook URL 은 dev 전용 테스트 채널.
3. 같은 댓글이 prod / dev 양쪽에서 폴링됨.
4. prod 는 운영 채널에 `[ENV: prod]` 로 1 회 노출.
5. dev 는 dev 테스트 채널에 `[ENV: dev]` 로 1 회 노출.
6. **운영 채널에는 1 회만 노출됨을 확인** (L2 가 작동).

### 7.3 dev 가 운영 webhook URL 을 잘못 등록한 사고 (L2 무력화)

1. dev DB 의 routing_domain webhook URL 을 prod 채널로 잘못 등록.
2. dev 인스턴스에서 `FIGMA_SYNC_DISCORD_ENABLED=false` 인 정상 dev 설정.
3. dev 가 분류는 돌리지만 L3 가드로 발송 skip → prod 채널 오염 0.
4. dispatch / cursor 는 갱신 안 됨 (L3 가 발송 안 한 댓글의 dispatch 행을 만들지 않음).
5. dev 의 분류 캐시 (`figma_comment_classification`) 는 정상 적재 — 검증 가능.

### 7.4 dev 가 L1 + L3 모두 켠 사고 (최종 시나리오)

1. dev 가 `FIGMA_SYNC_ENABLED=true` + `FIGMA_SYNC_DISCORD_ENABLED=true`.
2. dev DB 의 webhook URL 은 dev 채널 (L2 정상).
3. dev → dev 채널에 발송. footer 에 `[ENV: dev]`.
4. 운영 채널 오염 0. 사고 = 0.

### 7.5 모든 안전망이 무력화된 worst case

1. dev 가 L1 + L3 모두 true + dev DB 에 prod webhook URL 잘못 등록.
2. dev → 운영 채널에 발송됨. footer 에 `[ENV: dev]`.
3. 운영자가 footer 라벨을 보고 "이건 dev 가 보낸 거다" 즉시 인지 → 5 분 안에 dev 인스턴스 정지 + 휴먼 에러 수정.
4. 사고 영향: 운영 채널에 잘못된 메시지 1 개 노출 (지속 X). 운영자 신뢰는 다소 깎이지만 데이터 손상 / 발송 폭주는 없음.

이 5 시나리오가 모두 통과하면 다층 방어가 의도대로 작동.

## 8. 참고

- 관련 ADR / 보고서
    - [ADR-003: Figma 댓글 → Discord 포워딩](../adr/003-figma-comment-discord-forwarder.md)
    - [ADR-004: Figma 시간창 단일 유즈케이스](../adr/004-figma-comment-time-window-unification.md) — dispatch / cursor 가드 도입.
    - [Figma ↔ LLM 캐시 구조 분석](Figma_LLM_캐시_구조_분석.md) — 본 계획의 L3 가 dispatch / cursor 와 어떻게 상호작용하는지.
    - [Figma 댓글 Discord 포워딩 보고서](Figma_댓글_Discord_포워딩_보고서.md) — 운영 가이드 amendment 대상.
- 핵심 코드
    - [FigmaCommentSyncScheduler](../../src/main/java/com/umc/product/figma/adapter/in/scheduler/FigmaCommentSyncScheduler.java) — L1 (`enabled` 검사) 위치.
    - [FigmaCommentSummaryService](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentSummaryService.java) — L3 (`discordEnabled` 검사) 도입 위치.
    - [DiscordMentionWebhookAdapter](../../src/main/java/com/umc/product/figma/adapter/out/external/DiscordMentionWebhookAdapter.java) — L4 (footer 환경 라벨) 도입 위치.
    - [FigmaSyncProperties](../../src/main/java/com/umc/product/figma/adapter/out/external/FigmaSyncProperties.java) — `discordEnabled` 필드 추가.
- 외부 자료
    - [Spring Boot Profile-specific properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.files.profile-specific) — application-{profile}.yml.
