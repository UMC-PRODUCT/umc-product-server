# Figma ↔ LLM 도메인 간 캐싱 구조 분석

> 작성일: 2026-05-08
> 대상: 현재 figma 댓글 분류 경로의 모든 캐시 / 호출 가드 / 외부 캐시-인접 메커니즘.
> 목적: 운영자/신규 합류자가 "한 댓글이 들어왔을 때 캐시가 어떻게 비교/저장되는가" 를 한 장에서 추적할 수 있도록 정리.

본 문서는 결함 분석이 아니라 **현재 시점의 구조 도식**이다. 결함과 권고는 별도 보고서 [LLM_분류_캐시_점검_보고서](LLM_분류_캐시_점검_보고서.md) 가 다룬다.

## 0. TL;DR

`figma` 도메인의 댓글 분류는 **3-tier 캐시 + 호출 가드** 구조로 LLM 호출 횟수를 0 회로 수렴시키는 것을 목표로 한다.

| 계층 | 위치 | 키 | 값 | 수명 |
|------|------|----|----|------|
| **L1** | `FigmaClassificationCachePort` → 전역 `CacheUseCase` → Caffeine adapter | `commentId` | `ClassificationCacheValue` (positive + negative) | 5 분 (yaml 조정) |
| **L2** | `figma_comment_classification` 테이블 | `comment_id` UNIQUE | `domain_key` + `provider` + `classified_at` | 영구 (positive + non-mock 만) |
| **L3** | LLM batch 호출 (`ChatCompleteUseCase`) | — | LLM 응답 | 단발 |

옆으로 붙는 호출 가드 / 캐시-인접 인프라:

| 인프라 | 위치 | 역할 |
|--------|------|------|
| `LlmRateLimiter` | `llm` 도메인 | 사전 페이싱 (token bucket, 분당 RPM + burst). 토큰 부족 시 `Thread.sleep`. |
| `LlmCallGuard` | `llm` 도메인 | 회로 차단. 연속 실패 N 회 → 일정 시간 호출 자체 거부. |
| `LlmFallbackConfig` | `llm` 도메인 | 활성 어댑터가 없으면 mock 으로 자동 fallback. |
| Caffeine `recordStats` + `CaffeineCacheMetrics` | `global/cache` adapter | hit/miss/eviction 메트릭을 `figma.classifier.l1.*` 로 노출. |

## 1. 도메인 경계와 책임

```
figma 도메인                                    llm 도메인
┌─────────────────────────────────────────┐    ┌──────────────────────────────────────┐
│ FigmaCommentSummaryService              │    │ ChatCompleteUseCase                  │
│   └─ FigmaCommentDomainClassifier ──────┼───▶│   └─ ChatCompletionService           │
│      ├ L1: FigmaClassificationCachePort │    │      ├ LlmCallGuard.allow()          │
│      │       → global CacheUseCase      │    │                                      │
│      ├ L2: LoadFigmaCommentClassPort   │    │      ├ LlmRateLimiter.acquire()      │
│      │       (DB)                       │    │      ├ ChatCompletionPort.complete() │
│      └ L3: chatCompleteUseCase          │    │      │   (Mock | Gemini | OpenAI |   │
│                                         │    │      │    GoogleGenAI)               │
│                                         │    │      └ LlmMetrics.record*()          │
│                                         │    │ LlmFallbackConfig                    │
│                                         │    │   (mock 어댑터로 안전 fallback)        │
└─────────────────────────────────────────┘    └──────────────────────────────────────┘
```

**경계의 핵심 규칙:**

1. `figma` 가 캐시 정책의 주체다. L1 도메인 정책은 `FigmaClassificationCachePort` wrapper가 소유하고, 저장소 구현은 `global/cache` adapter가 맡는다. L2 는 `figma` 도메인이 소유한다.
2. `llm` 도메인은 캐시를 모른다 — 항상 LLM 호출을 수행한다는 가정에서 동작한다 (`ChatCompletionService` 안에는 결과 캐시 자체가 없다).
3. `figma` → `llm` 의 입력은 `ChatCompleteCommand` (system + user prompt + 선택적 maxTokens override). prompt 내용 자체는 figma 가 완전히 완성해 보낸다.
4. `llm` → `figma` 의 출력은 `ChatCompletionResult(text, provider, promptTokens, completionTokens)`. `provider` 가 캐시 정책 (mock 응답 영구 저장 차단) 의 결정 신호로 쓰인다.

이 경계 덕분에 `llm` provider 를 mock → Gemini → OpenAI 로 교체해도 figma 캐시 동작은 영향받지 않는다 (단, mock 응답 식별을 `provider == "mock"` 문자열에 의존).

## 2. 호출 트리

### 2.1 정기 sync (스케줄러 + admin syncAll)

```
FigmaCommentSyncScheduler.poll()
  └─ SummarizeFigmaCommentsUseCase.summarize(scheduledSync(from, to))
     └─ FigmaCommentSummaryService.summarize(...)
        └─ for each watched file:
           └─ FigmaCommentDomainClassifier.classifyBatch(filtered, candidateKeys)
              ├─ L1 lookup (Figma cache port) ← cachePort.contains/get(commentId)
              │      hit (Optional present)  → results 에 put
              │      hit (Optional empty)    → "negative cache" — results 에 안 들어감, LLM 미호출
              │      miss                    → afterMemoryCache 에 추가
              ├─ L2 lookup (DB IN 쿼리)        ← loadClassificationPort.findClassifications(ids)
              │      hit  → cache.put(positive), results 에 put
              │      miss → uncached 에 추가
              └─ L3 호출 (uncached 가 비어있지 않으면)
                 └─ doBulkClassify(uncached, candidates)
                    ├─ chatCompleteUseCase.complete(BATCH_SYSTEM_PROMPT, user, maxTokens)
                    └─ catch LlmDomainException → BulkClassifyOutcome(Map.of(), null) 반환
                 (호출 결과 처리)
                 ├─ callSucceeded = bulk.provider() != null
                 ├─ if callSucceeded: cache.put(commentId, Optional.ofNullable(domain))
                 │       (positive 면 results 에 put + persistIfEligible 로 L2 적재)
                 └─ if !callSucceeded: cache 미설정 → 다음 사이클에 재시도
```

### 2.2 admin digest (force=true)

`SummarizeFigmaCommentsCommand.digest(from, to)` 흐름은 위와 동일하지만 **dispatch dedup 을 무시** 하고 cursor 도 advance 하지 않는다. 캐시 동작 자체는 sync 와 같다.

### 2.3 admin preview (dryRun=true)

`SummarizeFigmaCommentsCommand.preview(...)` / `previewSingleFile(...)` 도 캐시 동작은 동일하다. 다만 발송 / dispatch / cursor 갱신을 모두 건너뛴다. **L2 적재 (`persistIfEligible`) 도 그대로 일어난다** — preview 호출이 분류 결과를 영구 캐시에 흘리는 부수효과가 있다는 것이 의도된 동작 (prompt 호출 한 번이면 운영 비용 모델상 영구 캐시 적재가 이득).

## 3. 캐시 정책 매트릭스

각 응답 종류가 L1 / L2 어디에 쓰이는지를 한 표로 정리.

| 응답 종류 | provider 값 | L1 (`FigmaClassificationCachePort`) | L2 (DB) | 다음 사이클 동작 |
|-----------|-------------|---------------|---------|------------------|
| 후보 매칭 (정상 분류) | non-mock (e.g. `vertexai-gemini`) | positive 적재 | positive 적재 | L1/L2 hit 으로 LLM 미호출 |
| 후보 매칭 (mock provider) | `mock` | positive 적재 | **저장 안 함** | 5분 내 L1 hit, 이후엔 L1 expire → L2 miss → LLM 재호출 |
| 후보 외 응답 (LLM 정상 호출) | non-mock | **negative 적재** (`Optional.empty()`) | **저장 안 함** | 5분 내 L1 hit (negative), 이후 L1 expire → 다시 LLM 재호출 |
| LLM 호출 자체 실패 (`LlmDomainException`) | `null` | **저장 안 함** (P1 fix) | 저장 안 함 | 다음 호출에서 즉시 LLM 재시도 |
| 회로 차단 (`LlmCallGuard.allow()` false) | — (LLM 미호출) | 저장 안 함 | 저장 안 함 | 회로 차단 만료 후 첫 호출에서 LLM 재시도 |

핵심 invariant 두 개:

- **mock 응답은 L2 에 절대 들어가지 않는다.** `persistIfEligible` 가 `provider == "mock"` 을 거른다. 검증 단계의 임시 분류가 운영에 영구 누적되는 사고를 막는다.
- **transient 호출 실패는 캐시되지 않는다 (P1 fix 이후).** 이전에는 5분 negative cache 로 박혀 일시적 LLM outage 가 5분 부분 정전으로 증폭되었다.

## 4. 진입점별 호출 가드 / 캐시-인접 인프라

`figma` 의 캐시는 LLM 호출 횟수를 줄이지만, 호출이 실제로 일어날 때의 wall-clock latency 와 비용은 다음 가드들이 결합해 통제한다.

### 4.1 `LlmCallGuard` (회로 차단)

- 연속 실패 임계치 (`failureThreshold`, 기본 5) 도달 시 일정 시간 (`openDurationMillis`, 기본 60s) 호출 자체를 거부.
- 거부 시 `ChatCompletionService` 가 `LlmErrorCode.CHAT_COMPLETION_FAILED` 예외를 즉시 던진다.
- `figma` 측에서는 `doBulkClassify` 의 catch 가 받아서 `BulkClassifyOutcome(Map.of(), null)` 로 변환 → 위 §3 의 "호출 자체 실패" 행과 동일하게 cache 미박힘.
- 결과: 회로 차단 동안 figma 측 분류는 모두 fallback 도메인으로 라우팅되며, 회로 복구 직후 다음 사이클에서 정상 분류가 재개된다.

### 4.2 `LlmRateLimiter` (token bucket)

- `app.llm.rate-limit.requests-per-minute` (기본 10) + burst (기본 5) 로 사전 페이싱.
- 토큰 부족 시 `Thread.sleep` 으로 caller thread 대기. caller (스케줄러) 가 다른 일을 못 함.
- 캐시는 아니지만 "LLM 호출 횟수" 를 통제한다는 점에서 캐시와 같은 문제를 다른 면으로 푼다.

### 4.3 `LlmFallbackConfig` (어댑터 fallback)

- `LLM_PROVIDER` 가 잘못된 값이거나 활성 provider 의 자격이 부족해 ChatCompletionPort 빈이 만들어지지 못하면, `MockChatCompletionAdapter` 를 안전 fallback 으로 등록한다.
- fallback 진입은 부팅 시 WARN 로그 + `llm.active.provider.info{provider="mock", fallback="true"}` gauge 로 상시 노출된다.
- 운영자는 메트릭만으로 "지금 mock 으로 동작 중인가" 를 즉시 확인할 수 있다.

## 5. 외부에 노출된 운영 surface

### 5.1 환경변수 / yaml

```yaml
app:
  llm:
    provider: ${LLM_PROVIDER:mock}                  # mock | openai | vertexai-gemini | google-genai
    model: ${LLM_MODEL:gemini-2.5-flash-lite}
    temperature: ${LLM_TEMPERATURE:0.0}
    max-output-tokens: ${LLM_MAX_OUTPUT_TOKENS:32}
    rate-limit:
      requests-per-minute: ${LLM_RATE_LIMIT_RPM:10}
      burst: ${LLM_RATE_LIMIT_BURST:5}
    circuit-breaker:
      failure-threshold: 5                         # 코드 기본값
      open-duration-millis: 60000                  # 코드 기본값

  figma:
    classifier:
      cache:
        max-size: ${FIGMA_CLASSIFIER_CACHE_MAX_SIZE:10000}
        ttl: ${FIGMA_CLASSIFIER_CACHE_TTL:PT5M}
```

### 5.2 메트릭 (Prometheus / actuator)

| 메트릭 | 라벨 | 의미 |
|--------|------|------|
| `llm_chat_completion_seconds` | `provider`, `status` | LLM 호출 latency 히스토그램 |
| `llm_chat_completion_total` | `provider`, `status` | LLM 호출 카운트. status = `success` / `failed` / `circuit-open`. |
| `llm_chat_completion_tokens_total` | `provider`, `type` | 토큰 사용량. type = `in` / `out`. |
| `llm_active_provider_info` | `provider`, `fallback` | 항상 1. 라벨로 활성 provider 와 fallback 진입 여부 표시. |
| `figma_classifier_l1_size` | — | 현재 L1 캐시 항목 수 |
| `figma_classifier_l1_gets` | `result=hit\|miss` | L1 lookup 카운트 |
| `figma_classifier_l1_puts` | — | L1 put 카운트 |
| `figma_classifier_l1_evictions` | `cause=size\|expired\|...` | L1 eviction 카운트 |

기본 PromQL 패턴:

```promql
# L1 hit rate
rate(figma_classifier_l1_gets{result="hit"}[5m])
  / rate(figma_classifier_l1_gets[5m])

# fallback adapter 활성 여부 (1 이면 fallback 중)
sum(llm_active_provider_info{fallback="true"})
```

### 5.3 DB 진단 SQL

```sql
-- 영구 캐시 적재 상태
SELECT provider, COUNT(*), MIN(classified_at), MAX(classified_at)
FROM figma_comment_classification
GROUP BY provider;

-- 특정 댓글 강제 무효화 (재분류 강제)
DELETE FROM figma_comment_classification WHERE comment_id = '...';

-- 보존 기간 정책: dispatch 와 달리 figma_comment_classification 은 회수 잡 없음 (영구).
-- 도메인 키 변경 / fallback 배치가 잘못 학습된 경우 운영자가 직접 정리.
```

현재 L1 저장소는 in-memory Caffeine 이라 운영자가 직접 호출할 수 있는 무효화 수단은 process 재시작뿐이다. 전역 `CacheUseCase.evict`는 이미 있으므로, admin endpoint가 도입되면 `CacheNamespace.FIGMA_CLASSIFICATION` + `commentId`로 단건 무효화를 연결할 수 있다.

## 6. cross-domain 의 미세한 결합 지점

`figma` 와 `llm` 의 경계는 깔끔해 보이지만 다음 3 가지가 약한 결합을 만든다.

### 6.1 mock provider 식별이 문자열 매칭

`FigmaCommentDomainClassifier.MOCK_PROVIDER = "mock"` 과 `MockChatCompletionAdapter.PROVIDER_NAME = "mock"` 이 동일한 문자열이라야 L2 영구 캐시 차단이 동작한다. 새 provider 가 도입될 때 이름이 `mock-anything` 으로 바뀌면 차단이 풀린다. 의존이 implicit 하지만 부팅 시 활성 provider 가 INFO 로그에 노출되고 gauge 라벨로도 보이므로 회귀 시 즉시 보인다.

### 6.2 LLM 호출 실패가 figma 측 cache 정책의 입력

`doBulkClassify` 의 catch 블록이 `BulkClassifyOutcome(Map.of(), null)` 로 반환하는 시그널을 호출자 (`classifyBatch`) 가 `bulk.provider() == null` 로 검사한다. provider 가 null 이라는 사실 자체가 "호출 자체 실패" 의 도메인 신호로 사용된다.

이 의미는 `FigmaCommentDomainClassifier` 안에 갇혀 있어 외부에 노출되지 않지만, llm 도메인이 미래에 `ChatCompletionResult` 의 provider 를 nullable 로 다른 의미로 쓰면 figma 가 깨진다.

### 6.3 mock 응답이 figma 시점에는 정상 응답으로 보임

`MockChatCompletionAdapter.complete` 는 user prompt 의 앞 80자를 `[mock] ...` 로 echo 한다. figma 분류 시점에서는 응답이 후보 도메인 키 중 하나가 아니므로 negative cache 로 박힌다. fallback 도메인으로만 라우팅되어 운영자가 fallback 채널이 가득 차는 현상으로 인지하게 된다.

이는 의도된 동작이다 — mock 모드에서 운영자가 figma flow 의 fallback path 자체를 검증할 수 있다. 다만 운영자가 fallback 도메인 / mock 모드 / 캐시 효과 셋을 한 번에 눈으로 구분하기 어려워, gauge 메트릭 (`llm_active_provider_info`) 으로 mock 활성 여부를 별도 채널로 노출한 것이다.

## 7. 최근 개선

다음 4 개 커밋이 본 구조의 운영 가시성과 안정성을 끌어올렸다.

| 커밋 | 변경 |
|------|------|
| `7c6e0c0b` | LLM 호출 자체 실패 시 negative cache 박지 않음 (P1 fix). 5분 부분 정전 위험 제거. |
| `aafa867e` | Caffeine `recordStats()` + `CaffeineCacheMetrics` 로 `figma.classifier.l1.*` 메트릭 노출. |
| `02ab5e24` | `FigmaClassifierProperties` 로 cache.max-size / cache.ttl 외부화. |
| `2733abec` | `llm.active.provider.info{provider, fallback}` gauge 로 활성 provider / fallback 진입 여부 상시 노출. |

2026-05-21 작업에서는 L1 캐시가 전역 cache adapter 위로 이동했다.

| 커밋 | 변경 |
|------|------|
| `8a5cb539` | `CacheUseCase`, `CacheStorePort`, `CacheNamespace`, `CacheSpec` 전역 캐시 계약 추가. |
| `6dce2c18` | Caffeine 저장소 adapter 추가. TTL, maximum size, namespace별 metric 등록을 전역 adapter에서 처리. |
| `54b9592a` | Figma classification 캐시를 `FigmaClassificationCachePort` wrapper를 통해 전역 cache usecase로 이전. |

본 구조는 ADR-008 (LLM 도메인 provider 전략) 의 비용 가정 (일 ~500 호출) 을 토대로 설계되었다. 호출량이 일 1000+ 로 늘어나면 후보 외 응답의 영구 negative 캐싱 / 분산 L1 / admin invalidate endpoint 같은 후속 작업이 필요해진다 (점검 보고서 §3.2, §3.4, §3.5).

## 8. 운영 플레이북

### 8.1 "어느 댓글이 왜 fallback 으로 갔는지" 추적

1. `figma_comment_classification` 에서 해당 commentId 가 있는지 확인.
   - 있으면 → `domain_key` 로 분류된 이력. 직전 정기 sync 가 dispatch 로 막혔거나 force 모드가 아니면 발송 안 됐을 것 (`figma_comment_dispatch` 확인).
   - 없으면 → L2 미스. mock 응답이거나 후보 외 응답이거나 호출 실패였음. 다음 단계.
2. 해당 commentId 를 admin preview API 로 다시 조회 (`GET /api/v1/admin/figma/preview?watchedFileId=...&from=...&to=...`).
   - 같은 fallback 으로 가면 → LLM 응답이 일관되게 후보 외다. 후보 도메인 키 자체를 점검 (`figma_routing_domain` 행과 commentId 의 의미 비교).
   - 정상 분류로 가면 → 직전엔 transient 실패였을 것. P1 fix 이후라 다음 사이클에 자동 회복.

### 8.2 "캐시가 안 도는 것 같다" 진단

1. `figma_classifier_l1_gets{result="hit"}` / 전체 = hit rate 확인. 정상 운영에서는 동일 댓글이 sync → preview → dispatch retry 에 반복 등장하므로 평균 0.5+ 가 나와야 함.
2. hit rate 가 0 에 가까우면:
   - L1 size 가 0 인지 확인 (`figma_classifier_l1_size`). 부팅 직후 빈 상태일 수 있음.
   - eviction 이 비정상적으로 많은지 (`figma_classifier_l1_evictions{cause="size"}`). max-size 가 작아 회전이 빠른 경우.
3. `llm_active_provider_info{fallback="true"}` 가 1 이면 mock fallback 진입 중 → 운영자 인증/자격을 점검 (`LLM_PROVIDER` 환경변수 + Vertex AI 자격).

### 8.3 잘못된 분류를 강제로 다시 시도

```sql
DELETE FROM figma_comment_classification WHERE comment_id = '<id>';
```
+ application 재시작 (현재 L1 invalidate 우회). 이후 admin endpoint 가 생기면 `CacheUseCase.evict(CacheNamespace.FIGMA_CLASSIFICATION, CacheKey.from(commentId))`로 단건 무효화 가능.

## 9. 참고

- 관련 ADR
    - [ADR-003: Figma 댓글 → Discord 포워딩](../adr/003-figma-comment-discord-forwarder.md) — 분류 도메인 라우팅 도입.
    - [ADR-004: Figma 시간창 단일 유즈케이스](../adr/004-figma-comment-time-window-unification.md) — sync 진입점 통합.
    - [ADR-008: LLM 도메인 provider 전략](../adr/008-llm-domain-provider-strategy.md) — 호출량 / 비용 가정 + Spring AI 채택.
    - [ADR-012: LLM 호출 동기 대기 병목 완화](../adr/012-llm-call-blocking-bottleneck-mitigation.md) — caller thread 차단 완화 후속 전략.
- 기존 보고서
    - [LLM 분류 캐시 점검 보고서](LLM_분류_캐시_점검_보고서.md) — 본 구조의 결함 분석 + 개선 권고 (P1 hotfix 포함).
- 핵심 코드
    - [FigmaCommentDomainClassifier](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifier.java)
    - [FigmaClassifierProperties](../../src/main/java/com/umc/product/figma/application/service/FigmaClassifierProperties.java)
    - [FigmaClassificationCacheAdapter](../../src/main/java/com/umc/product/figma/adapter/out/cache/FigmaClassificationCacheAdapter.java)
    - [CaffeineCacheStoreAdapter](../../src/main/java/com/umc/product/global/cache/adapter/out/CaffeineCacheStoreAdapter.java)
    - [CacheNamespace](../../src/main/java/com/umc/product/global/cache/domain/CacheNamespace.java)
    - [FigmaCommentClassificationPersistenceAdapter](../../src/main/java/com/umc/product/figma/adapter/out/persistence/FigmaCommentClassificationPersistenceAdapter.java)
    - [ChatCompletionService](../../src/main/java/com/umc/product/llm/application/service/ChatCompletionService.java)
    - [LlmMetrics](../../src/main/java/com/umc/product/llm/application/service/LlmMetrics.java)
    - [LlmCallGuard](../../src/main/java/com/umc/product/llm/application/service/LlmCallGuard.java)
    - [LlmRateLimiter](../../src/main/java/com/umc/product/llm/application/service/LlmRateLimiter.java)
    - [LlmFallbackConfig](../../src/main/java/com/umc/product/llm/adapter/out/external/LlmFallbackConfig.java)
- 통합 마이그레이션: [V2026.05.07.10.00__create_figma_tables.sql](../../src/main/resources/db/migration/V2026.05.07.10.00__create_figma_tables.sql)
