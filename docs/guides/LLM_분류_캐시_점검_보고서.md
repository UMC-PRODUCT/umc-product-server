# Figma LLM 분류 캐시 점검 보고서

> 작성일: 2026-05-08
> 대상: `com.umc.product.figma.application.service.FigmaCommentDomainClassifier` 의 3-tier 캐시 구성과 부속 설정.
> 목적: "캐시가 제대로 동작하지 않는 것 같다" 는 관측에 대한 원인 분석 + 개선 권고.

## 0. TL;DR

3-tier 캐시 골격 자체는 잘 짜여 있다. 다만 **bulk LLM 호출이 통째로 실패할 때 미캐시 댓글들을 5분간 negative 캐싱** 하는 결함이 있어, 일시적 LLM outage 가 그대로 "5분간 모든 신규 댓글이 fallback 채널로만 발송" 으로 이어진다. 운영자가 체감하는 "캐시가 이상하다" 의 가장 유력한 원인이다.

이외에 (i) 운영 가시성 (hit rate / fallback 상태) 노출이 없고, (ii) 운영자가 잘못된 분류를 수동 무효화할 수단이 없으며, (iii) 다중 인스턴스 환경에서 L1 이 인스턴스별로 격리되어 동일 댓글에 LLM 이 중복 호출될 여지가 있다.

P1 (negative cache poisoning) 만 단독 핫픽스로 즉시 처리 가능하고, 나머지는 후속 개선 항목.

## 1. 현재 구성 요약

### 1.1 캐시 계층

| 계층 | 위치 | 키 | 값 | 정책 |
|------|------|----|----|------|
| L1 | [`FigmaCommentDomainClassifier.cache`](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifier.java) (Caffeine) | `commentId` | `Optional<domainKey>` (positive + negative) | `expireAfterWrite=5min`, `maximumSize=10_000` |
| L2 | `figma_comment_classification` 테이블 | `comment_id` (UNIQUE) | `domain_key` + `provider` + `classified_at` | 영구 보관, **positive 만**, `mock` provider 응답은 저장 안 함 |
| L3 | `ChatCompleteUseCase.complete(...)` | — | LLM 응답 | 마지막 수단. batch prompt 로 단일 호출에 N개 묶음. |

### 1.2 호출 경로 (production)

```
FigmaCommentSummaryService.summarize(...)
  → FigmaCommentDomainClassifier.classifyBatch(comments, candidateDomainKeys)
      ├ L1 (Caffeine): cache.getIfPresent(commentId)
      │      hit (Optional present)  → results 에 put, 다음으로 진행
      │      hit (Optional empty)   → "negative 캐시" — results 에 안 들어감, 그러나 LLM 재호출도 안 함
      │      miss (null)             → afterMemoryCache 에 추가
      ├ L2 (DB): loadClassificationPort.findClassifications(ids)
      │      hit  → cache.put(positive), results 에 put
      │      miss → uncached 에 추가
      └ L3 (LLM batch): doBulkClassify(uncached, candidates)
             각 댓글에 대해:
                cache.put(Optional.ofNullable(domain))   ← 문제 지점 (§3.1)
                domain ≠ null 이면 results 에 put + persistIfEligible
```

production 진입점은 `classifyBatch` 한 곳뿐이다 (`classify` 단건은 테스트만 사용).

### 1.3 운영 설정

| 설정 키 | 기본값 | 비고 |
|--------|------|------|
| `app.llm.provider` | `mock` (default) | `vertexai-gemini` / `google-genai` / `openai` 중 활성 어댑터 1개만 로드 |
| `app.llm.rate-limit.requests-per-minute` | `10` | provider 당 페이싱. token bucket. |
| `app.llm.circuit-breaker.failure-threshold` | `5` | 연속 실패 5회 → 60초 차단 |
| Caffeine `maximumSize` | `10_000` (hardcoded) | yaml 노출 없음 |
| Caffeine `expireAfterWrite` | `5min` (hardcoded) | yaml 노출 없음 |
| `figma_comment_classification` 보존 기간 | 무제한 | 회수 잡 없음 (dispatch 와 다름) |

### 1.4 fallback 설계 (의도된 동작 vs 실제 동작)

[`LlmFallbackConfig`](../../src/main/java/com/umc/product/llm/adapter/out/external/LlmFallbackConfig.java) 가 있어, `LLM_PROVIDER` 가 비정상이거나 활성 provider 의 자격 자동구성이 실패하면 **자동으로 mock 어댑터로 fallback** 한다 + WARN 로그.

운영진 입장에서는 "분류가 random 으로 보임" + "cache 가 무력해 보임" 이 동시에 발생할 수 있다. 이는 fallback 상태 자체가 문제가 아니라, fallback 진입 사실이 메트릭/대시보드로 명확히 노출되지 않는 게 문제 (§3.6).

## 2. 정상 동작이 검증된 항목

여기는 검토 결과 **이상 없음** 으로 분류된 부분이다. 사용자가 의심한 "캐시가 안 도는데?" 가 이 영역이라면 아래 검증을 보고 배제 가능하다.

- ✅ **L1 positive 캐시는 단위 테스트로 검증됨** ([`FigmaCommentDomainClassifierTest.캐시_히트_시_LLM_재호출_없음`](../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L62)). 동일 commentId 두 번째 호출이 LLM 을 다시 부르지 않는다.
- ✅ **L2 → L1 hydrate 도 검증됨** (`DB_캐시_히트_시_LLM_미호출`). DB 에 행이 있으면 LLM 미호출.
- ✅ **batch 부분 캐시 혼합도 검증됨** (`batch_분류_캐시_혼합`, `batch_분류_DB_부분_히트`). 캐시된 댓글은 prompt 에서 제외되고 미캐시만 호출됨.
- ✅ **DB 영구 캐시의 동시성 race 흡수**: [`FigmaCommentClassificationPersistenceAdapter.save`](../../src/main/java/com/umc/product/figma/adapter/out/persistence/FigmaCommentClassificationPersistenceAdapter.java#L37) 가 `existsByCommentId` 가드 + `DataIntegrityViolationException` catch 둘 다 보유. unique index 가 정합성 보장.
- ✅ **mock provider 응답이 영구 캐시에 안 새는 안전장치**: [`persistIfEligible`](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifier.java#L204) 가 `MOCK_PROVIDER.equalsIgnoreCase` 로 mock 응답 차단. 통합 테스트 부재라 검증은 코드 리뷰 수준.
- ✅ **schema 측의 NOT NULL 제약**: `domain_key` 가 NOT NULL 이라 "후보 외 응답" 이 영구 저장될 수 없음. 잘못된 분류가 영구화될 위험은 없다.

## 3. 발견된 결함

### 3.1 P1 — bulk LLM 호출 실패 시 negative cache 5분간 박힘 (실제 운영 영향) ⚠️

**위치**: [`FigmaCommentDomainClassifier.classifyBatch`](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifier.java#L138-L162)

**증상**: `chatCompleteUseCase.complete(...)` 가 `LlmDomainException` 으로 실패하면 [`doBulkClassify`](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifier.java#L189) 가 `BulkClassifyOutcome(Map.of(), null)` 을 반환. 이어서 `classifyBatch` 의 후속 루프:

```java
if (!uncached.isEmpty()) {
    BulkClassifyOutcome bulk = doBulkClassify(uncached, candidateDomainKeys);
    for (FigmaCommentInfo c : uncached) {
        String domain = bulk.results().get(c.commentId());     // 항상 null (실패 시)
        cache.put(c.commentId(), Optional.ofNullable(domain)); // 모든 댓글에 negative cache 박힘
        ...
    }
}
```

→ **uncached 안의 모든 commentId 가 5분간 L1 에 negative 로 캐싱** 된다. 다음 사이클 (5분 안) 에 같은 댓글이 들어오면 L1 hit 으로 처리되어:
- LLM 재시도가 일어나지 않는다.
- `results` 에 안 들어가므로 `FigmaCommentSummaryService` 입장에서는 "분류 결과 없음" → fallback 도메인으로 라우팅.

**운영 영향**: provider 측 일시 outage / rate limit / 자격 만료 같은 transient 실패가 즉시 5분 짜리 부분 정전으로 증폭된다. 같은 사이클의 댓글 K 건이 모두 fallback 채널에 쌓인다.

**근거**: 단위 테스트 [`LLM_실패_시_null_negative_캐시`](../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L94-L105) 가 이 동작을 **의도** 로 검증하고 있다 — 즉 negative 캐싱이 설계 의도이긴 하나, **실패 원인을 구분하지 않는** 게 문제다. "후보 외 응답" 은 negative 캐싱이 합리적이지만 "호출 자체 실패" 는 transient 라 캐싱하면 안 된다.

**권고 수정**:
```java
if (!uncached.isEmpty()) {
    BulkClassifyOutcome bulk = doBulkClassify(uncached, candidateDomainKeys);
    boolean callSucceeded = bulk.provider() != null;  // doBulkClassify 의 catch 블록은 provider=null 로 표현
    for (FigmaCommentInfo c : uncached) {
        String domain = bulk.results().get(c.commentId());
        if (callSucceeded) {
            // 호출 자체는 성공 — domain null 은 "후보 외 응답" 이므로 negative 캐싱이 합리적
            cache.put(c.commentId(), Optional.ofNullable(domain));
        }
        // 호출 실패면 cache.put 자체를 건너뛰어 다음 사이클에 재시도되도록 한다
        if (domain != null) {
            results.put(c.commentId(), domain);
            persistIfEligible(c.commentId(), domain, bulk.provider());
        }
    }
}
```

같은 패턴을 [`doClassify`](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifier.java#L183) 의 `LlmDomainException` catch 블록에도 적용해야 한다 — 현재는 catch 가 그냥 null 반환만 하고, 호출 측이 `cache.put(commentId, Optional.empty())` 하므로 동일 패턴.

수정과 함께 새 테스트 추가:
- "LLM 호출 자체 실패 시 negative cache 가 박히지 않아 다음 호출에서 LLM 재시도된다."
- "LLM 응답이 후보 외 값이면 negative cache 가 박혀 같은 commentId 의 5분 내 재호출에서 LLM 미재시도."

### 3.2 P2 — "후보 외 응답" 이 5분마다 무한 재호출됨 (비용 문제, 정확성은 OK)

L2 는 positive 만 영구 보관. "후보 외 응답" 은 L1 에만 negative 로 5분 보관. 따라서 같은 commentId 가 6분 이상 간격으로 다시 들어오면 L1 expire → L2 miss → LLM 재호출.

운영 패턴상 한 번 분류 실패한 댓글은 보통 다시 분류해도 같은 결과 (LLM 응답 일관성, 후보 도메인 변경 드묾) 를 낸다. 그런데 5분 expire 후 LLM 호출이 다시 발생.

ADR-003 amendment / 코드 주석은 "검증 단계의 임시 분류가 운영에 영구 누적되지 않게 한다" 를 이유로 든다. 합리적이지만, 실제 효과로는 **같은 commentId 가 매 5분마다 LLM 호출 1건씩 소비** 한다. 호출량이 일 500건 이상으로 늘면 비용 모델 가정 (ADR-008) 을 흔들 수 있다.

**권고**: 즉시 변경하지 않음. 호출량이 일 1000건 넘는 시점부터 "negative 응답을 별도 영구 테이블 (`figma_comment_classification_misses`) 에 보관해 영구 negative 캐시" 를 검토. 또는 dispatch 가드를 활용해 "이미 fallback 으로 발송된 댓글은 분류 재시도 안 함" 을 추가.

### 3.3 P3 — Caffeine 캐시의 hit/miss 메트릭 노출 안 됨

Caffeine 빌더에 `recordStats()` 가 없다. 운영자는 hit rate / miss rate / eviction 횟수를 알 수 없어, "캐시가 잘 도는지" 를 직접 측정할 수단이 없다.

**권고**:
```java
this.cache = Caffeine.newBuilder()
    .maximumSize(CACHE_MAX_SIZE)
    .expireAfterWrite(CACHE_TTL)
    .recordStats()  // 추가
    .build();

// MeterBinder 로 Micrometer 에 노출
@Bean
MeterBinder figmaClassifierCacheMetrics(FigmaCommentDomainClassifier classifier) {
    return registry -> CaffeineCacheMetrics.monitor(registry, classifier.cache(), "figma.classifier.l1");
}
```

[`LlmMetrics`](../../src/main/java/com/umc/product/llm/application/service/LlmMetrics.java) 와 같은 prometheus 패턴으로 통일.

### 3.4 P3 — 운영자가 캐시를 무효화할 수단 없음

DB (L2) 는 SQL `DELETE FROM figma_comment_classification WHERE comment_id = '...'` 로 강제 무효화 가능하지만, L1 (Caffeine) 은 process 재시작이 유일한 방법.

운영자가 "이 commentId 의 분류가 잘못 됐어, 강제로 다시 분류" 를 못 한다. 같은 댓글이 fallback 으로 가는 상황을 디버깅하기 어렵다.

**권고**: admin 진입점 추가
```
DELETE /api/v1/admin/figma/llm-cache/{commentId}
```
→ L1 에서 invalidate + L2 에서 row 삭제. 운영진이 잘못 학습된 분류를 즉시 재시도시킬 수 있다.

### 3.5 P4 — 다중 인스턴스 환경 가시성 (현재 영향 작음)

L1 은 instance-local. 인스턴스 N개 운영하면 같은 commentId 가 동시에 들어왔을 때 N개 인스턴스가 각각 L1 miss → L2 가 hydrate 되어 있으면 거기서 막히지만, **L2 도 miss** 인 첫 사이클이라면 N개 인스턴스가 동시에 LLM 을 호출한다.

DB UNIQUE 가 영구 저장 시점의 race 만 막아주고, 이미 발생한 호출은 막지 못한다. 호출량이 작아서 (일 500건) 현재는 재정 영향 없음.

**권고**: 즉시 조치 안 함. 다중 인스턴스 운영이 상시화되면 그때 분산 캐시 (Redis L1.5) 도입 검토. ADR-012 의 비동기화와 같이 평가하면 좋다.

### 3.6 P3 — fallback adapter 활성 사실이 메트릭에 노출되지 않음

`LlmFallbackConfig` 가 mock 어댑터로 fallback 되면 WARN 로그 1회만 남고, 이후 운영자는 "분류가 random 처럼 보이는데 cache 도 안 도는 것 같다" 만 관측한다. fallback 상태가 메트릭으로 항상 노출되어야 한다.

**권고**:
- `LlmCallGuard` / `LlmRateLimiter` 와 별도로, `ChatCompletionService` 에 활성 provider 이름을 gauge metric 으로 항상 노출 (`llm.active.provider` 라벨).
- fallback 어댑터 진입 시 `llm.fallback.engaged` 라벨이 "true" 인 gauge 를 게시.

### 3.7 P4 — Caffeine 설정이 hardcoded

`CACHE_MAX_SIZE = 10_000`, `CACHE_TTL = Duration.ofMinutes(5)` 가 코드 상수. yaml 로 외부화되어 있지 않아 운영 중 튜닝이 불가.

**권고**: `app.figma.classifier.cache.max-size`, `app.figma.classifier.cache.ttl` 로 빼서 운영 환경별로 조정 가능하게.

### 3.8 P5 — 단건 `classify()` 가 production 에서 미사용 (그러나 negative cache 동일 결함)

`classifyBatch` 만 production 호출 경로다. 단건 `classify()` 는 테스트만 사용하지만 동일한 negative caching 결함을 보유. P1 수정 시 단건 경로도 같이 정정해 코드 일관성 유지.

## 4. 우선순위 요약

| ID | 이슈 | 심각도 | 즉시 조치 권장 |
|----|------|--------|----------------|
| 3.1 | bulk 호출 실패 시 5분 negative cache poisoning | **P1** | ✅ 단독 hotfix |
| 3.2 | "후보 외 응답" 이 5분마다 LLM 재호출 | P2 | ⏳ 호출량 증가 시 |
| 3.3 | Caffeine 메트릭 미노출 | P3 | ✅ 운영 가시성 작은 PR |
| 3.4 | 운영자 수동 invalidate 수단 없음 | P3 | ⏳ admin 진입점 정책 결정 후 |
| 3.5 | 다중 인스턴스 L1 분리 | P4 | ❌ 현재 호출량에 영향 작음 |
| 3.6 | fallback adapter 메트릭 미노출 | P3 | ✅ 메트릭 PR 에 묶음 |
| 3.7 | Caffeine 설정 hardcoded | P4 | ⏳ 운영 튜닝 필요 시 |
| 3.8 | 단건 `classify()` 도 동일 결함 | P5 | ✅ P1 hotfix 와 같이 |

## 5. 권고 단계 (PR 단위)

### Phase A (즉시, 1 PR)
- 3.1 P1 핫픽스: `classifyBatch` / `classify` 의 negative cache 분기를 "호출 성공 시에만 negative 캐시" 로 수정.
- 3.8 P5: 같은 패턴을 단건 `classify()` 에도 적용.
- 단위 테스트 2건 추가 (호출 실패 시 재시도 vs 후보 외 응답 시 negative 캐시).

### Phase B (1~2주 안, 1 PR)
- 3.3 P3: Caffeine `recordStats()` + Micrometer binder.
- 3.6 P3: 활성 provider / fallback 상태 gauge metric 노출.
- 3.7 P4: `app.figma.classifier.cache.*` yaml 외부화.

### Phase C (필요 시, 별도 ADR)
- 3.4 P3: admin invalidate endpoint (운영 정책 결정 후).
- 3.2 P2: "후보 외 응답" 영구 negative 캐싱 (호출량 일 1000+ 시).
- 3.5 P4: 분산 L1 (Redis) 도입 (다중 인스턴스 상시 운영 시).

## 6. 참고

- 관련 ADR: [ADR-003](../adr/003-figma-comment-discord-forwarder.md), [ADR-004](../adr/004-figma-comment-time-window-unification.md), [ADR-008](../adr/008-llm-domain-provider-strategy.md), [ADR-012](../adr/012-llm-call-blocking-bottleneck-mitigation.md)
- 핵심 코드:
    - [FigmaCommentDomainClassifier](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifier.java)
    - [FigmaCommentClassificationPersistenceAdapter](../../src/main/java/com/umc/product/figma/adapter/out/persistence/FigmaCommentClassificationPersistenceAdapter.java)
    - [LlmFallbackConfig](../../src/main/java/com/umc/product/llm/adapter/out/external/LlmFallbackConfig.java)
- 통합 마이그레이션: [V2026.05.07.10.00__create_figma_tables.sql](../../src/main/resources/db/migration/V2026.05.07.10.00__create_figma_tables.sql)
- 단위 테스트: [FigmaCommentDomainClassifierTest](../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java)

## 7. 검증 절차 (P1 핫픽스 머지 후)

1. local 에서 mock provider 로 띄움 → `app.llm.provider=mock` 그대로 두고 `MockChatCompletionAdapter` 가 `LlmDomainException` throw 하도록 일시적으로 바꿔 테스트.
2. figma sync 1회 트리거 → fallback 채널로 모두 발송됨을 확인.
3. mock 어댑터 throw 제거 후 즉시 sync 재트리거 → 같은 댓글이 정상 분류되는지 확인 (fix 적용 전이라면 5분 negative cache 로 인해 fallback 채널로 또 감).
4. 정상 분류되면 P1 fix 검증 완료.

추가로 운영 환경에서는 `figma_comment_classification` 테이블의 `classified_at` 분포를 주기적으로 확인해 "오래된 분류가 영구 누적" 또는 "최근 분류가 갱신 안 됨" 같은 패턴 변화를 감지한다.
