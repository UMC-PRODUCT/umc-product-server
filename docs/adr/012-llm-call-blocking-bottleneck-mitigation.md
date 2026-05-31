# ADR-012: LLM 호출의 동기 대기 병목 완화 전략

## Status

Proposed (2026-05-08)

## Context

본 ADR 작성 시점(2026-05-08) 기준 LLM 호출은 다음 동기 경로를 따른다.

```
스케줄러 / admin API
  → FigmaCommentDomainClassifier.classifyBatch(comments, candidates)
    → (L1 Caffeine miss → L2 DB miss →) ChatCompleteUseCase.complete(...)
      → ChatCompletionService.complete(...)
        → LlmCallGuard.allow()           // 회로 차단 체크
        → LlmRateLimiter.acquire()       // 토큰 부족 시 Thread.sleep
        → ChatCompletionPort.complete(...)
          → SpringAiGeminiChatCompletionAdapter.complete(...)
            → ChatClient.builder(chatModel).prompt()....call().chatResponse()  // 외부 HTTP 동기 호출
```

이 구조에서 다음 병목이 누적된다.

1. **caller thread 의 직접 차단**: [`ChatCompletionService.complete`](../../src/main/java/com/umc/product/llm/application/service/ChatCompletionService.java#L70) 가 동기 메서드라, 호출자(스케줄러 단일 스레드 또는 Tomcat worker) 가 외부 LLM 응답이 올 때까지 그대로 멈춘다. Gemini Flash 계열은 평균 1~3 초, 입력이 길거나 모델이 바쁘면 10초 이상도 정상 범주.
2. **`LlmRateLimiter` 의 sleep 추가**: token bucket 이 소진되면 `Thread.sleep` 으로 대기한다 ([파일](../../src/main/java/com/umc/product/llm/application/service/LlmRateLimiter.java#L67)). 외부 latency 위에 페이싱 대기까지 누적된다.
3. **스케줄러 주기 침범**: [`FigmaCommentSyncScheduler.poll`](../../src/main/java/com/umc/product/figma/adapter/in/scheduler/FigmaCommentSyncScheduler.java) 는 `@Scheduled(fixedDelayString = "${app.figma.sync.poll-interval}")` 다. fixed delay 라 직전 호출이 끝난 뒤 다음 호출이 시작되므로, LLM 호출이 길어지면 다음 사이클이 그만큼 밀린다. 5분 폴링이 사실상 7~8분 주기가 될 수 있다.
4. **운영자 대면 latency**: admin 의 `POST /api/v1/admin/figma/digest`, `GET /api/v1/admin/figma/preview` 는 LLM 분류가 끝날 때까지 HTTP 응답을 보내지 않는다. 운영자는 화면이 멈춘 것처럼 느낀다.
5. **단일 인스턴스 단일 스레드 가정**: `LlmCallGuard` 와 `LlmRateLimiter` 는 in-memory 상태라 인스턴스 격리만 보장하며, 다중 인스턴스 환경에서는 전체 호출 한도를 정확히 통제하지 못한다. 동시성 확장이 단순히 스레드를 늘리는 것만으로는 안전하지 않다.

호출량의 절대 규모는 ADR-008 §Context 에서 추정된 그대로 작다 (sync 250건/일 + preview/digest 트리거 포함 ~500건/일). 즉 RPS 부담은 거의 없고, **개별 호출의 wall-clock latency 가 caller 경험에 1:1 로 노출되는 구조** 가 핵심 문제다.

이 ADR이 결정해야 하는 것은 다음이다.

- LLM 호출이 caller thread 를 직접 차단하지 않게 만드는 방법.
- 운영자 대면 admin API 응답성을 개선하는 방법.
- 위 두 가지를 도입할 때의 비용 (인프라/복잡도/운영 가시성).
- 도입 순서 (한 번에 vs 단계적).

## Decision

우리는 다음 3 단계 전략을 단계적으로 채택하기로 결정한다. **Phase 1 만 즉시 채택하고, Phase 2·3 은 운영 데이터 확보 후 검토** 한다.

### Phase 1 (즉시 채택): caller 분리 — virtual thread 기반 비동기 실행

`ChatCompleteUseCase` 의 동기 시그니처는 그대로 유지하되, **분류 호출의 실제 실행은 dedicated virtual-thread executor 에서 수행** 한다. 호출자는 `CompletableFuture<ChatCompletionResult>` 를 받아 다른 작업을 진행하다가 결과를 collect 한다.

- 신규 진입점: `ChatCompleteUseCase.completeAsync(ChatCompleteCommand)` — `CompletableFuture` 반환.
- 기존 `complete(...)` 는 deprecate 하지 않고 유지 (단건 사용처 호환).
- `FigmaCommentDomainClassifier.classifyBatch` 는 batch prompt 를 그대로 사용하되, 호출 자체를 비동기로 보내고 동일 사이클의 다른 파일 처리와 병렬로 진행한다 (스케줄러 단일 스레드 안에서도 wall-clock 단축).
- Java 21 의 virtual threads 를 사용한 `Executors.newVirtualThreadPerTaskExecutor()` 로 thread pool 부담 없이 동시성 확보.

### Phase 2 (3~6 개월 후 검토): admin API 비동기 응답 패턴

운영자 대면 latency 가 운영 신뢰의 1차 변수로 식별되면 추가한다.

- `POST /api/v1/admin/figma/digest`, `GET /api/v1/admin/figma/preview` 가 LLM 응답 대기 없이 즉시 200/202 + `jobId` 반환.
- 결과는 (a) polling endpoint `GET /api/v1/admin/figma/jobs/{jobId}` 또는 (b) SSE 로 client 가 구독.
- 분류 결과를 보관할 임시 결과 저장소 (`figma_summary_job` 테이블) 도입.

### Phase 3 (장기): 백그라운드 사전 분류

batch sync 사이클 자체에서 LLM 을 부르는 구조를 폐기하고, **댓글이 처음 fetch 될 때 비동기 작업 큐에 분류 작업을 등록** 해 미리 결과를 [`figma_comment_classification`](../../src/main/java/com/umc/product/figma/domain/FigmaCommentClassification.java) (L2 캐시) 에 적재한다.

- sync 사이클은 항상 L2 cache hit 만 만나므로 LLM 대기가 0 으로 수렴.
- 작업 큐는 신규 인프라 도입 (Redis/RabbitMQ) 없이 DB 기반 outbox 로 시작 가능.
- ADR-004 의 `figma_summary_cursor` / `figma_comment_dispatch` 와 자연스럽게 fit.

## Alternatives Considered

### 1. 현행 동기 유지

장점:

- 변경 비용 0. 기존 caller 들이 결과를 즉시 받는 가장 단순한 모델.
- 트랜잭션 컨텍스트 / 에러 전파 / 로그 흐름이 직선적이라 디버깅이 쉽다.

단점:

- caller thread 가 외부 LLM 응답 시까지 차단되어 wall-clock latency 가 caller UX 에 그대로 노출된다.
- 스케줄러 주기 침범 (5분 → 7~8분) 이 운영 데이터 확보가 누적될수록 더 두드러진다.
- admin preview / digest 호출 시 화면 멈춤이 운영자 신뢰를 깎는다.

선택하지 않은 이유:
호출량 자체는 작지만 호출 1건의 wall-clock 이 운영 UX 와 스케줄러 주기에 직접 노출되는 구조라, 동기 유지의 비용이 점점 누적된다.

### 2. Phase 1 (virtual thread async) 채택 ✅

장점:

- caller thread 가 즉시 풀려 스케줄러 다음 사이클이 정상 주기를 회복한다.
- Java 21 의 virtual threads 라 thread pool 인프라 추가 부담 없음. blocking I/O 가 자연스럽게 스케일된다.
- 변경 폭이 LLM 도메인 service 시그니처 추가 + figma classifier 호출부 1~2 곳에 한정.
- 기존 `LlmRateLimiter` / `LlmCallGuard` 로직은 그대로 보존된다 (executor 안에서 실행).

단점:

- 동기 시그니처 (`complete`) 와 비동기 시그니처 (`completeAsync`) 가 공존해 호출자가 둘 중 하나를 골라야 한다.
- 비동기 결과 collect 시 예외 전파 패턴 (`join()` vs `get()` vs `exceptionally`) 을 figma classifier 가 일관되게 적용해야 한다.
- virtual thread 안에서 `ThreadLocal` (Spring Security context, MDC 등) 가 자동 전파되지 않으므로 logging context 손실 가능 — `TaskDecorator` 적용으로 보완.

선택한 이유:
즉시 도입 가능한 가장 작은 변경으로 가장 큰 효과 (caller thread 차단 해제). 호출량이 작아 thread pool 폭주 우려가 없고, virtual thread 라 추가 인프라도 없다.

### 3. Phase 2 (admin API 비동기 응답) 즉시 도입

장점:

- 운영자 대면 latency 가 즉시 200ms 이하로 떨어진다 (jobId 반환만).
- 장기 분류 작업이 HTTP timeout / nginx idle timeout 에 걸리는 사고를 원천 차단.

단점:

- 신규 결과 저장소 (`figma_summary_job`) + polling/SSE endpoint + 프론트 변경이 동시에 필요.
- Phase 1 만 도입해도 caller thread 차단은 해제되므로, admin UX 가 실제로 운영 신뢰의 1차 변수인지 데이터로 검증한 뒤 도입하는 것이 합리적.

선택하지 않은 이유 (이번 ADR 시점):
Phase 1 의 효과를 먼저 측정한 뒤, 운영자 latency 가 여전히 문제라면 Phase 2 를 별도 ADR 로 진행한다. 동시 도입은 실패 격리가 어려워 운영 위험을 키운다.

### 4. Phase 3 (백그라운드 사전 분류) 즉시 도입

장점:

- sync 사이클의 LLM 대기를 0 으로 수렴시키는 가장 근본적 해결책.
- 분류 결과가 이미 dispatch 시점에 L2 에 적재되어 있어 운영자 preview 도 즉시 응답.

단점:

- 작업 큐 / outbox / worker 가 신규 인프라로 추가된다 — 모니터링, 실패 재처리, 백프레셔 정책을 모두 새로 정의해야 함.
- "댓글 fetch 시점" 을 정의하기 모호하다. 현재는 sync 사이클 안에서만 댓글이 fetch 되므로, 사전 분류를 위해 별도 fetch 잡이 필요해진다 (이중 잡 운영).
- 배치 분류로 도입한 효율이 단건 분류로 회귀해 LLM 호출 수가 늘 수 있다 — 비용 모델 재검토 필요.

선택하지 않은 이유 (이번 ADR 시점):
구조적으로 가장 깔끔하지만 인프라 비용이 크고, Phase 1·2 가 없는 상태에서 단독 도입할 만큼 운영 데이터가 누적되어 있지 않다. 향후 호출량이 일 수천 건 이상으로 늘면 별도 ADR 로 다시 평가한다.

### 5. Streaming 응답 (Spring AI ChatClient.stream)

장점:

- 첫 토큰 latency 가 빨라진다.
- preview UX 에서 점진적 결과 표시 가능.

단점:

- 분류 시맨틱은 단일 키워드 (또는 N×{commentId, domainKey} JSON) 만 필요해 streaming 의 점진적 출력 이점이 거의 없다.
- batch 분류 응답 (JSON 배열) 은 모두 받기 전에 파싱이 불가능하다 — 사실상 동기와 동일한 wall-clock.
- ChatClient.stream() 결과의 메트릭/회로 차단 통합이 추가 코드 부담.

선택하지 않은 이유:
분류 도메인의 응답 모양 (짧은 단일 출력 또는 JSON 배열) 이 streaming 과 fit 하지 않는다. 본 ADR 의 병목 (caller thread 차단) 도 streaming 으로는 해결되지 않는다.

### 6. Provider race (다중 provider 동시 호출 후 fastest-wins)

장점:

- p99 latency 가 두 provider 의 minimum 으로 수렴해 tail latency 가 줄어든다.

단점:

- 호출 비용이 2배 (또는 N배) 가 된다. ADR-008 의 비용 모델 가정 (분당 수십 건) 으로도 매월 비용이 의미 있게 늘어난다.
- 두 provider 의 응답 형식 정규화 비용이 추가된다.
- 정확도가 다른 두 provider 가 서로 다른 답을 반환할 때 어느 것을 채택할지 (fastest? majority?) 정책이 필요.

선택하지 않은 이유:
호출량 절대치가 작아서 비용 2배가 절대 금액으로는 작더라도, 단일 provider race 만으로는 caller thread 차단이라는 본질적 문제를 해결하지 못한다 (race 도 결국 동기 join 이 필요).

### 7. Local 한국어 분류 모델 (KoBERT / KoSimCSE 등) 직접 호스팅

장점:

- LLM 호출 latency 가 10~50ms 수준으로 떨어져 본 ADR 의 모든 병목이 사실상 사라진다.
- 외부 API 의존이 줄고 데이터가 외부로 나가지 않는다.

단점:

- 모델 서빙 인프라 (GPU 또는 충분한 CPU, 모델 파일 배포, 메모리 1~2GB 추가) 가 신규 운영 책임으로 추가된다.
- 한국어 fine-tuning 데이터셋 확보 + 평가 + 정확도 검증이 필요. 운영자 신뢰 수준에 도달하기까지의 검증 비용이 크다.
- ADR-008 시점의 결정 (Spring AI 기반 외부 provider) 을 부분 supersede 해야 한다.

선택하지 않은 이유:
호출량 규모 (일 ~500건) 와 정확도 요구치 (운영자 신뢰) 를 같이 고려하면, 외부 LLM API 의 정확도 우위가 인프라 비용을 상쇄한다. 호출량이 일 수만 건 단위로 늘 때 다시 평가한다.

### 8. Provider Batch API (Gemini batch / OpenAI Batch)

장점:

- 호출 단가가 50% 수준. 비용 절감.
- 대량 호출에 최적화.

단점:

- SLA 가 24시간이라 사이클 5분 운영과 fit 하지 않는다.
- 분류 결과를 다음 사이클까지 미루면 운영자 알림이 24h 지연된다 — 본 시스템 도입 동기 (ADR-003 §1 알림 누락 해소) 와 정면 충돌.

선택하지 않은 이유:
SLA 가 운영 요구와 1~2 자릿수 다르다. 사후 통계 분석 등 별도 use case 가 생기면 그때 도입한다.

### 9. Prompt 캐시 (provider 측)

장점:

- 동일/유사 prompt 의 재호출 비용 / 일부 latency 절감.

단점:

- 본 시스템은 매 호출의 prompt 가 거의 항상 다르다 (batch 댓글 본문이 매번 변경). prompt 캐시 hit rate 가 0 에 가까움.
- system prompt 만 cacheable 한데 분류 system prompt 는 ~200 토큰이라 절감 효과가 미미.

선택하지 않은 이유:
호출 패턴상 hit rate 가 거의 없다.

### 10. 더 작은/빠른 모델 (gemini-2.5-flash-lite → 더 작은 변종)

장점:

- 응답 latency 가 1~2초 단축될 가능성. 비용도 추가 절감.

단점:

- 한국어 분류 정확도가 떨어질 수 있다. 운영자 신뢰의 핵심 변수라 충분한 평가 없이 교체 불가.
- 모델 변경은 ADR-008 의 구현 영역이라 본 ADR 의 결정과는 직교.

선택하지 않은 이유 (이번 ADR 범위 밖):
본 ADR 은 호출 동시성 / 비동기 구조에 한정하고, 모델 자체 교체는 ADR-008 의 후속 작업으로 분리한다. 다만 Phase 1 도입 후 정확도 손실 없이 latency 만 줄일 여지는 항상 검토 가치가 있다.

## Consequences

### Positive

- Phase 1 도입 즉시 caller thread 차단이 해제되어 figma 스케줄러 사이클이 정상 주기를 회복한다 (5분 → 5분 그대로).
- admin sync trigger 의 응답이 빨라져 운영자가 화면 멈춤을 덜 느낀다 (응답 자체는 결과 모음을 기다리지만 internal blocking 시간이 줄어 timeout 위험이 낮아짐).
- 같은 사이클의 여러 파일을 동시에 분류 호출에 보낼 수 있어, 활성 watched file 이 N 개여도 wall-clock 이 N 배 늘지 않는다 (rate limiter 가 허용하는 한도까지 동시 진행).
- Phase 1 의 비동기 실행 인프라가 Phase 2 / Phase 3 의 토대가 되어, 후속 ADR 채택 시 신규 인프라 도입 부담이 줄어든다.

### Negative

- `complete` / `completeAsync` 두 시그니처가 공존해 호출자가 어느 것을 써야 하는지의 결정 비용이 생긴다 — javadoc 으로 가이드 명시.
- virtual thread 안에서 MDC / Spring Security context / OpenTelemetry trace 컨텍스트가 자동 전파되지 않을 수 있어, log/관측 추적이 끊길 위험. `TaskDecorator` 또는 명시적 context propagation 필수.
- 비동기 분류의 예외가 `CompletionException` 으로 wrapping 되어, 기존 `LlmDomainException` catch 패턴이 한 단계 unwrap 을 요구한다.
- 기존 단위 테스트가 동기 모델 가정으로 작성되어 있어, 비동기 경로에 대한 테스트가 추가 필요.
- 다중 인스턴스 환경에서 `LlmRateLimiter` / `LlmCallGuard` 가 인스턴스 단위로만 격리되는 한계는 본 ADR 로도 해결되지 않는다 (분산 rate limit / 분산 회로 차단은 별도 ADR 로 분리).

### Neutral / Trade-offs

- Phase 1 만 도입해 admin API 응답 latency 의 절대값은 크게 줄지 않는다 (LLM 응답 대기 자체는 여전히 발생). 운영자 UX 에 결정적 차이가 필요하면 Phase 2 가 후속으로 필요.
- virtual threads 는 Java 21 의 비교적 신기능으로, JFR / 디버거 / 일부 라이브러리의 지원 정도가 platform thread 만큼 성숙하지 않다. 운영 중 이슈 추적 시 platform thread 보다 도구 의존 비용이 약간 더 든다.
- 비동기 도입은 코드 가독성을 일부 떨어뜨린다. 동기 코드의 직선성과 비교해 호출자가 future chain / await 패턴을 추가로 익혀야 한다.
- Phase 3 (백그라운드 사전 분류) 으로 가면 LLM 호출 시점이 댓글 fetch 시점으로 분리되어, 본 시스템의 dispatch / cursor 시맨틱과의 정합성을 다시 정의해야 한다. 이는 별도 ADR 의 주요 결정사항이 된다.

## Implementation Notes

### Phase 1 도입 단계

1. `LlmExecutorConfig` 신설: `@Bean Executor llmExecutor` = `Executors.newVirtualThreadPerTaskExecutor()`. `TaskDecorator` 로 MDC 컨텍스트 전파.
2. `ChatCompleteUseCase.completeAsync(ChatCompleteCommand)` 신규 메서드 추가, `CompletableFuture<ChatCompletionResult>` 반환.
   ```java
   default CompletableFuture<ChatCompletionResult> completeAsync(ChatCompleteCommand command) {
       return CompletableFuture.supplyAsync(() -> complete(command), llmExecutor);
   }
   ```
   기본 구현은 동기 `complete` 를 executor 위에 얹는 형태로 시작. 추후 native non-blocking adapter 가 도입되면 override.
3. `FigmaCommentDomainClassifier.classifyBatchAsync(...)` 신규 메서드 추가, `CompletableFuture<Map<String, String>>` 반환. 기존 `classifyBatch` 는 `classifyBatchAsync(...).join()` 으로 위임.
4. `FigmaCommentSummaryService.summarize(...)` 의 파일별 loop 를 `CompletableFuture.allOf(...)` 패턴으로 묶어, 활성 watched file 들을 동시에 분류 호출에 보낸다. `LlmRateLimiter` 가 자동으로 burst 를 조절한다.
5. 단위 테스트: `FigmaCommentSummaryServiceTest` 에 동시 호출 시나리오 추가 (3 파일 동시 분류, 모두 성공 시 wall-clock < 단일 호출 × 3 검증).

### TaskDecorator 패턴 (MDC / Tracing 전파)

```java
public final class ContextPropagatingTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> mdcSnapshot = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (mdcSnapshot != null) MDC.setContextMap(mdcSnapshot);
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
```

OpenTelemetry context 는 `Context.taskWrapping(executor)` 로 별도 wrap.

### 환경 설정 (제안)

```yaml
app:
  llm:
    async:
      enabled: ${LLM_ASYNC_ENABLED:true}     # Phase 1 활성화 토글
      max-concurrency: ${LLM_ASYNC_MAX_CONCURRENCY:8}  # rate limiter 와 함께 burst 상한 보강
```

`max-concurrency` 는 rate limiter 가 이미 있어 형식상 보호 장치이지만, virtual thread 자체는 한도가 없으므로 혹시 모를 폭주를 막기 위한 semaphore 로 적용한다.

### 측정 지표 (Phase 1 도입 후 1~2 주 관찰)

- `llm_call_latency_ms` (provider 별 p50/p95/p99) — 외부 호출 latency 자체는 변하지 않음을 확인.
- `figma_sync_cycle_duration_ms` — 사이클 wall-clock 이 의도대로 줄었는지.
- `llm_async_pool_size` / `llm_async_active_threads` — virtual thread 활성도.
- `llm_call_failed_total` (회로 차단 / rate limit / provider 오류 분리) — 비동기화로 새 실패 패턴이 생기지 않았는지.

위 지표가 안정적이면 Phase 2 검토 단계로 넘어간다.

### 운영 시 주의사항

- 비동기 도입 후 예외는 `CompletionException.getCause()` 로 unwrap 한 뒤 기존 `LlmDomainException` 분기를 적용해야 한다. classifier 호출부에서 일관되게 처리하지 않으면 silent failure 가 날 수 있다.
- `TaskDecorator` 를 빠뜨리면 log 의 traceId / userId 가 끊긴다. 비동기 적용 시 반드시 검증 (로그 한 사이클 추적해 traceId 가 일관된지 확인).
- 분산 rate limit 미도입 상태에서 instance scaling 을 늘리면 외부 provider 의 RPM 한도를 넘길 수 있다. Phase 1 단독 채택 시점에는 단일 인스턴스 운영 가정이 유지되어야 한다.

## References

- 관련 ADR
    - [ADR-003: Figma 댓글 → Discord 포워딩](003-figma-comment-discord-forwarder.md) — LLM 분류가 도입된 출발점.
    - [ADR-004: Figma 시간창 단일 유즈케이스 통합](004-figma-comment-time-window-unification.md) — sync 사이클의 진입점 / dispatch / cursor 구조. Phase 3 의 정합성 검토 대상.
    - [ADR-008: LLM 도메인 구현 (Spring AI + Gemini)](008-llm-domain-provider-strategy.md) — 본 ADR 의 호출량/비용 가정의 근거.
- 기존 코드
    - [ChatCompletionService](../../src/main/java/com/umc/product/llm/application/service/ChatCompletionService.java) — Phase 1 의 신규 메서드 추가 위치.
    - [LlmRateLimiter](../../src/main/java/com/umc/product/llm/application/service/LlmRateLimiter.java) — 비동기화 후에도 그대로 보존되는 burst 페이싱.
    - [FigmaCommentDomainClassifier](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifier.java) — caller 측 비동기 도입 1순위.
    - [FigmaCommentSummaryService](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentSummaryService.java) — 파일별 loop 의 동시화 대상.
- 외부 자료
    - [JEP 444: Virtual Threads (Java 21)](https://openjdk.org/jeps/444)
    - [Spring AI ChatClient streaming/blocking 차이](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
    - [Project Reactor `Mono.fromFuture` (Phase 2 후보 시 참고)](https://projectreactor.io/docs/core/release/reference/)
