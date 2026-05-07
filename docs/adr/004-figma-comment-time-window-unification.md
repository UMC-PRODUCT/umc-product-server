# ADR-004: Figma 댓글 동기화를 시간창 기반 단일 유즈케이스로 통합한다

## Status

Proposed (2026-05-07)

## Context

ADR-003 (및 amendment 1·2차) 시점에 Figma 댓글 → Discord 포워딩은 다음 세 진입점으로 확장되었다.

- **정기 sync (스케줄러 + admin trigger)**: [`FigmaCommentSyncCommandService`](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentSyncCommandService.java) → [`FigmaCommentBatchProcessor.processSyncCycle`](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentBatchProcessor.java#L54) → 파일별 `last_synced_comment_id` 이후의 댓글만 필터링하고, 발송 후 boundary 를 advance 한다.
- **digest catch-up (admin)**: [`FigmaCommentDigestService`](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentDigestService.java) → `FigmaCommentBatchProcessor.processDigestWindow` → `createdAt ∈ [from, to]` 필터, sync state 비변경.
- **preview (admin)**: [`FigmaCommentPreviewQueryService`](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentPreviewQueryService.java) → `last_synced_comment_id` 이후 댓글을 발송 없이 도메인 묶음 형태로 응답.

이 구조에는 다음 중복/마찰이 있다.

1. **`Mode.SYNC` / `Mode.DIGEST` 이중 분기**: `FigmaCommentBatchProcessor.filter()` 가 모드별로 두 가지 시간 기준(`last_synced_comment_id` boundary vs `createdAt` window)을 분기 처리한다. 같은 batch 처리 코드를 두 input 시맨틱이 공유한다.
2. **`last_synced_comment_id` 의 의미 과부하**: 한 컬럼이 (i) "어디까지 처리했는가" 라는 진행 상태와 (ii) "어디부터 발송할 것인가" 라는 발송 가드 두 가지 책임을 동시에 진다. preview 는 (i) 만 필요하고, digest 는 (ii) 와 무관해야 하는데, 모두 같은 컬럼을 직접 참조한다.
3. **시간창과 boundary 의 시맨틱 차이**: `last_synced_comment_id` 는 "이 ID 다음부터" 를 의미하므로, Figma REST 응답 정렬에 의존하고(서버가 정렬 보장을 약화하면 누락/중복 위험), Figma 측 댓글 reorder/소프트삭제를 표현하지 못한다. 반면 createdAt 시간창은 입력값으로 결정되는 순수 필터이므로 멱등하다.
4. **3-tier 분류 캐시는 이미 시간창 호출에 친화적**: [`FigmaCommentDomainClassifier`](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifier.java) 의 L1 (Caffeine 5분 TTL) + L2 (`figma_comment_classification` 영구 캐시) + L3 (LLM batch) 구조 덕분에, 같은 commentId 가 여러 시간창에 다시 들어와도 LLM 호출이 추가로 발생하지 않는다. 따라서 "시간창을 단위로 호출한다" 는 시맨틱은 비용 관점에서도 추가 부담이 없다.
5. **운영 멘탈모델의 단순화 여지**: 운영진은 "최근 N분 사이의 댓글을 모아서 한 번에 본다" 라는 사고를 이미 갖고 있다 (digest API 도입 배경). 스케줄러도 동일한 시맨틱(`[now-Δ, now]`) 으로 재정의되면, sync / digest / preview 는 "시간창" 이라는 동일 모델 위의 세 가지 표현(발송함·발송함+force·발송 안 함) 으로 정렬된다.

이 결정에서 정해야 할 것은 다음과 같다.

- 정기 sync 의 진행 기준을 `last_synced_comment_id` (ID boundary) 에서 시간창 (`[lastWindowEnd, now]`) 으로 옮길지.
- 옮긴다면, sync / digest / preview 의 본체를 단일 유즈케이스로 합칠지.
- 시간창 기반으로 옮길 때 사라지는 "중복 발송 방지" 가드를 어떻게 대체할지 (창 겹침, 재시도, 스케줄러 다중 인스턴스 등 상황에서 댓글이 두 번 발송되지 않도록).
- "스케줄러가 해당 기간의 댓글을 요약한다" 의 정의 — 현재 도메인 묶음 embed 그대로인지, LLM 한 줄 요약 같은 신규 기능인지.

## Decision

우리는 다음과 같이 결정한다.

1. **단일 유즈케이스 통합**: Figma 댓글 처리의 본체는 `[from, to]` 시간창을 입력으로 받아 "활성 파일 전체에서 해당 창의 댓글을 모아 → 도메인별로 분류 → 도메인 묶음 embed 발송" 하는 단일 유즈케이스로 합친다. 이름은 `SummarizeFigmaCommentsUseCase` (in port). 본체 구현은 기존 `FigmaCommentBatchProcessor` 를 단일 경로로 축소한 `FigmaCommentSummaryService` (또는 동일 클래스의 단일 메서드) 가 담당한다.

2. **세 진입점은 시간창 + 부가 정책으로 정의**:
    - **스케줄러 sync**: `[lastWindowEnd, now]` 의 시간창으로 호출. `lastWindowEnd` 는 신규 도메인 상태 `figma_summary_cursor` 에서 읽고, 발송 성공 후 `now` 로 갱신한다.
    - **admin digest**: 운영진이 명시한 `[from, to]` 로 호출. `figma_summary_cursor` 는 변경하지 않는다 (catch-up / 회고용).
    - **admin preview**: 동일한 시간창 입력을 받지만 `dryRun=true` 옵션을 켜서 호출. Discord 발송 / cursor 갱신 / dispatch 기록을 모두 건너뛰고, 묶음 결과만 응답한다.

3. **중복 발송 방지: `figma_comment_dispatch` 테이블 도입**: `last_synced_comment_id` 의 발송 가드 책임은 신규 테이블 `figma_comment_dispatch (comment_id PK/UNIQUE, domain_id, dispatched_at)` 로 옮긴다.
    - 시간창 안의 댓글 중 이미 dispatch 행이 있는 commentId 는 발송 단계에서 제외한다 (preview 에는 그대로 노출).
    - 발송 성공 후 `(comment_id, domain_id, dispatched_at=now)` 를 insert 한다.
    - admin digest 는 기본적으로 dispatch 가 있는 댓글도 다시 보낸다 (`force=true` 가 기본). 즉 catch-up 시맨틱을 유지한다.
    - dispatch 테이블은 90일 보존이며, `dispatched_at` 인덱스 + 별도 정리 잡으로 회수한다.

4. **`figma_summary_cursor` 신규 테이블**: 단일 행 cursor(`last_window_end TIMESTAMP NOT NULL`) 을 유지한다. 다중 인스턴스 환경에서 `SELECT ... FOR UPDATE` 또는 ShedLock 으로 직렬화한다. 파일별 cursor 가 아니라 전역 cursor 를 채택하는 이유는 (i) Figma REST 가 파일 단위지만 도메인 묶음 발송이 cross-file 이고, (ii) 단일 cursor 가 운영진의 멘탈모델("어느 시각까지 봤는가")에 직접 매핑되기 때문이다.

5. **`figma_watched_file.last_synced_comment_id` 폐기 + figma 마이그레이션 4종 단일 파일 병합**:
    - 본 ADR 채택 시점에 figma 도메인의 모든 마이그레이션 (`V2026.05.07.10.00__create_figma_tables.sql`, `V2026.05.07.10.10__create_figma_routing_domain_tables.sql`, `V2026.05.07.10.20__drop_figma_part_route.sql`, `V2026.05.07.21.30__create_figma_comment_classification.sql`) 4종이 모두 develop / main 에 머지되지 않았고 feature 브랜치에만 존재한다.
    - 따라서 deprecate-then-drop / 점진적 마이그레이션 패턴 대신 **원본 4개 파일을 단일 `V2026.05.07.10.00__create_figma_tables.sql` 로 병합**하면서 다음을 같이 정리한다:
        1. `last_synced_comment_id` 컬럼은 처음부터 정의하지 않는다 (병합 시 생략).
        2. `figma_part_route` 는 처음부터 만들지 않는다 (V10.20 의 DROP 도 자연 소멸).
        3. `figma_routing_domain`, `figma_routing_domain_mention`, `figma_comment_classification` 정의를 같은 파일에 흡수.
        4. ADR-004 신규 테이블 (`figma_summary_cursor`, `figma_comment_dispatch`) 도 같은 파일에 추가.
    - 병합 파일 안에는 각 테이블 블록 위에 **어느 ADR 의 어떤 결정이 그 테이블의 책임인지** 를 주석으로 남긴다 (예: `-- ADR-004: 시간창 + 중복 발송 가드`). 이는 후속 합류자가 git blame 없이도 의도를 추적할 수 있게 한다.
    - `last_synced_at`, `last_error` 는 운영 모니터링 가치가 있어 유지한다. 의미는 "마지막으로 이 파일을 fetch 한 시각 / 마지막 fetch 에서 발생한 오류" 로 재정의한다.

6. **"요약" 의 정의는 현재의 도메인 묶음 embed 로 한정한다**: 본 ADR 의 "scheduler 가 해당 기간의 댓글을 요약한다" 는 ADR-003 amendment 2차에서 도입된 도메인 묶음 embed (cross-file fields[]) 를 의미한다. LLM 으로 자연어 한 줄 요약을 생성하는 기능은 별도 ADR 에서 다룬다. 본 ADR 의 변경은 분류 시점이 아니라 발송/필터 경로에 한정된다.

7. **3-tier 분류 캐시는 그대로 유지**: ADR-006 시맨틱(L1 in-memory + L2 DB + L3 LLM batch) 은 변경하지 않는다. 시간창 단위 호출이 같은 commentId 를 여러 번 분류 요청해도 L2 hit 으로 LLM 호출이 추가되지 않으므로, 본 ADR 의 변경이 LLM 비용을 늘리지 않는다.

## Alternatives Considered

### 1. 현행 유지 (`Mode.SYNC` / `Mode.DIGEST` 분기 그대로)

`FigmaCommentBatchProcessor.filter()` 의 이중 분기와 `last_synced_comment_id` 기반 boundary 를 그대로 둔다.

장점:

- 마이그레이션 비용이 없다.
- 이미 운영에서 검증된 흐름이며, dispatch 테이블 같은 신규 인프라가 필요 없다.
- 발송 가드는 단일 컬럼으로 해결되어 단순하다.

단점:

- preview / sync / digest 가 동일 본체를 공유하지만 입력 시맨틱은 모드별로 다르다. 신규 진입점(예: 단일 도메인만 발송, 특정 사용자 댓글만 추출 등) 추가 시 분기가 더 늘어난다.
- `last_synced_comment_id` 는 Figma REST 응답 정렬에 묵시 의존하므로, 정렬 가정이 깨지면 누락/중복이 동시에 발생할 수 있다 (현재까지는 createdAt asc 가 안정적이지만 보증된 계약이 아니다).
- 스케줄러 구현이 "신규 댓글이 있느냐" 기준이라 "이번 사이클에서 본 시간창" 같은 운영 메트릭을 만들기 어렵다.

선택하지 않은 이유:
중기적으로 진입점이 추가될수록 `Mode` 분기 비용이 누적되며, 시간창 시맨틱이 결국 더 일반적이다.

### 2. `last_synced_comment_id` 만 `last_synced_at` (timestamp) 으로 교체

컬럼 의미만 시간 기반으로 바꾸고, 진입점 분리는 유지한다.

장점:

- 변경 폭이 작다 (마이그레이션 1건, batch processor filter 변경 1건).
- Figma REST 정렬 의존성이 사라진다.

단점:

- `Mode.SYNC` / `Mode.DIGEST` / preview 의 분기는 여전히 남는다 (sync 는 cursor 갱신함, digest 는 안 함, preview 는 발송 안 함).
- 중복 발송 가드는 여전히 timestamp boundary 만으로 처리되어, 사이클 재시도/창 겹침 시 같은 댓글이 두 번 발송될 수 있다 (현재의 ID boundary 는 적어도 "마지막 ID 까지 처리됐다" 라는 강제는 줬다).
- "시간창" 이라는 일반 개념이 진입점 코드에 노출되지 않아, 이 ADR 의 동기였던 단일 유즈케이스 통합은 달성되지 않는다.

선택하지 않은 이유:
Figma REST 정렬 의존성은 해소되지만, 본 ADR 의 1차 동기인 "단일 유즈케이스화" 는 달성되지 않는다. 절반의 정리는 다음 변경을 더 비싸게 만든다.

### 3. dispatch 테이블 없이 시간창만으로

`figma_comment_dispatch` 를 도입하지 않고, `figma_summary_cursor.last_window_end` 만으로 중복 발송을 막는다.

장점:

- 신규 테이블이 1개 줄어든다.
- 단순한 sliding window: 각 사이클마다 `(last_window_end, now]` 를 처리하고 cursor advance.

단점:

- 사이클 실패 시 재시도가 어렵다. 발송 일부가 성공하고 일부가 실패한 상태에서 cursor 를 되돌리면 성공한 도메인이 다시 발송된다. cursor 를 advance 하면 실패한 도메인의 댓글이 누락된다.
- 시간창이 정확히 같은 사이클을 두 번 돌리는 시나리오 (운영진의 수동 sync trigger + 스케줄러 동시 실행) 에서 같은 댓글이 두 도메인 메시지에 들어간다.
- digest 와 sync 의 분리도 모호해진다 ("digest 가 cursor 를 안 옮긴다" 라는 규칙만으로 운영이 추론해야 함).

선택하지 않은 이유:
중복 발송이 운영진의 알림 신뢰를 즉시 무너뜨리는 1차 리스크였고, ADR-003 amendment 2차에서 이미 `last_synced_comment_id` 가 그 가드 역할을 했었다. 시간창 기반으로 옮기면 그 가드가 사라지므로, 동등 이상의 가드를 명시적으로 도입하는 것이 안전하다. dispatch 테이블 1개의 비용이 합리적이다.

### 4. "요약" 을 LLM 자연어 한 줄 요약으로 정의

스케줄러가 시간창의 댓글을 도메인별로 모은 뒤, 한 도메인의 댓글들을 LLM 으로 1~3줄 요약문으로 압축해 embed 본문에 같이 띄운다.

장점:

- 운영진이 댓글 본문을 일일이 보지 않고도 "이번 창에 디자인 도메인에서 무슨 논의가 있었는지" 한눈에 파악할 수 있다.
- 알림 피로도가 추가로 줄어든다.

단점:

- 분류와 별개의 LLM 호출 (요약 호출) 이 도메인 × 사이클마다 발생한다. 분류 캐시는 commentId 단위라 요약에는 그대로 쓸 수 없다.
- 요약 캐시 키 정책 (어떤 commentId 집합 → 어떤 요약문 매핑) 이 추가 결정 사항이다.
- 요약 품질 검증이 필요하고, 잘못된 요약은 분류 오류보다 영향이 크다 (운영진이 본문 대신 요약문을 신뢰하기 시작하면).

선택하지 않은 이유:
본 ADR 의 동기는 "시간창 시맨틱으로 진입점을 통합" 이며, 요약은 별도 의사결정이다. 캐시 정책/비용 모델/품질 가드를 함께 정해야 하므로 별도 ADR 로 분리한다. 본 ADR 은 인프라 정리에 집중하고, 자연어 요약은 후속 ADR (예: ADR-007 이후) 에서 다룬다.

## Consequences

### Positive

- `FigmaCommentBatchProcessor.filter()` 의 `Mode` 분기가 사라지고, 본체가 시간창 입력 1개 + 발송 정책(`dryRun`, `force`) 2개 플래그로 표현되어 코드가 단순해진다.
- preview / sync / digest 가 같은 결과 모양(도메인 묶음) 을 공유하므로, 운영진이 preview 결과를 보고 "그대로 보내라" 하면 sync 가 동일한 묶음을 발송한다는 보증이 강해진다.
- `figma_comment_dispatch` 가 도입되면서 "이 댓글이 언제 어느 도메인으로 발송됐는지" 가 단일 진실 원본이 된다. 알림 누락/중복 의심 시 SQL 한 줄로 확인 가능하다.
- 시간창 시맨틱이 일반화되어 향후 통계/리포트 (예: "지난주 디자인 도메인 댓글 N건") 같은 read-side 기능을 같은 인프라 위에서 만들 수 있다.
- L2 분류 캐시 덕분에 같은 시간창을 여러 번 처리해도 LLM 비용이 추가되지 않는다.

### Negative

- 마이그레이션 변경이 필요하다. develop/main 미머지 feature 브랜치 단계이므로 figma 도메인 마이그레이션 4종 (`V10.00`, `V10.10`, `V10.20`, `V21.30`) 을 단일 `V10.00` 파일로 병합하면서 `figma_summary_cursor` / `figma_comment_dispatch` 신설과 `last_synced_comment_id` 컬럼 삭제, `figma_part_route` 정의 누락 처리를 한 번에 적용한다. 별도 deprecate / drop 마이그레이션은 발생하지 않으며, 병합 파일에는 각 테이블 블록의 책임 ADR 을 주석으로 남긴다.
- 다중 인스턴스 환경에서 `figma_summary_cursor` advance 가 동시 실행되지 않도록 보장해야 한다. ShedLock 도입(또는 기존 lock 메커니즘 재사용) 이 필요하다.
- dispatch 테이블 회수 잡이 필요하다 (90일 보존). 회수 잡 누락 시 무한히 누적된다.
- `figma_summary_cursor` 가 하나뿐이므로, 관리 실수로 cursor 를 미래로 advance 하면 그 사이의 댓글이 통째로 누락된다. (ID boundary 는 "이후" 시맨틱이라 실수해도 더 적게 발송하는 쪽으로 기울었음.)
- 기존 운영 가이드/대시보드의 `last_synced_comment_id` 참조를 모두 갱신해야 한다.

### Neutral / Trade-offs

- "전역 cursor vs 파일별 cursor" 는 운영 단순성 ↔ 파일별 격리 사이의 trade-off 다. 본 ADR 은 전역을 택했지만, Figma 파일 수가 늘어나거나 파일별 SLA 가 달라지면 파일별 cursor 로 다시 분리될 수 있다.
- dispatch 테이블의 보존 기간 (90일) 은 알림 신뢰도(중복 방지) ↔ 디스크 비용 사이의 trade-off 다. 운영 데이터 확보 후 조정한다.
- preview 의 시맨틱은 변한다. 기존 preview 는 "마지막 sync 이후의 신규 댓글" 이지만, 신규 preview 는 "지정한 시간창의 댓글 (기본값: 최근 폴링 주기)". UI 가 노출하는 라벨/툴팁을 갱신해야 한다.
- digest API 의 기본 동작은 `force=true` 로 유지되지만, 명시적 `force=false` 옵션을 노출해 "아직 안 보낸 것만 보낸다" 는 제3의 모드도 만들 수 있다. 본 ADR 시점에는 도입하지 않는다.

## Implementation Notes

### 통합 후 도메인 / 응용 계층 형태

```
com.umc.product.figma/
├── domain/
│   ├── FigmaSummaryCursor.java          # 신규: 단일 행 전역 cursor
│   ├── FigmaCommentDispatch.java        # 신규: 발송 기록 (commentId UNIQUE)
│   ├── FigmaWatchedFile.java            # last_synced_comment_id 사용 제거
│   └── ...
├── application/
│   ├── port/in/
│   │   ├── SummarizeFigmaCommentsUseCase.java   # 신규 단일 진입점 (dryRun/force)
│   │   ├── SyncFigmaCommentsUseCase.java        # @Deprecated (위임만)
│   │   ├── DigestFigmaCommentsUseCase.java      # @Deprecated (위임만)
│   │   └── PreviewFigmaCommentsUseCase.java     # @Deprecated (위임만)
│   ├── port/out/
│   │   ├── LoadFigmaSummaryCursorPort.java
│   │   ├── SaveFigmaSummaryCursorPort.java
│   │   ├── LoadFigmaCommentDispatchPort.java
│   │   └── SaveFigmaCommentDispatchPort.java
│   └── service/
│       ├── FigmaCommentSummaryService.java      # 신규 본체 (Mode 분기 제거)
│       ├── FigmaCommentSyncCommandService.java  # SummarizeFigmaCommentsUseCase 호출하는 thin shim
│       ├── FigmaCommentDigestService.java       # 동일하게 thin shim
│       └── FigmaCommentPreviewQueryService.java # 동일하게 thin shim
```

### 신규 시그니처

```java
public interface SummarizeFigmaCommentsUseCase {
    FigmaSummaryResult summarize(SummarizeFigmaCommentsCommand command);
}

public record SummarizeFigmaCommentsCommand(
    Instant from,
    Instant to,
    boolean dryRun,         // true → Discord 발송 X, dispatch 기록 X, cursor advance X
    boolean force,          // true → dispatch 가 있어도 다시 발송
    boolean advanceCursor   // true → 발송 성공 시 figma_summary_cursor.last_window_end = to
) {
    public static SummarizeFigmaCommentsCommand scheduledSync(Instant from, Instant to) {
        return new SummarizeFigmaCommentsCommand(from, to, false, false, true);
    }
    public static SummarizeFigmaCommentsCommand digest(Instant from, Instant to) {
        return new SummarizeFigmaCommentsCommand(from, to, false, true, false);
    }
    public static SummarizeFigmaCommentsCommand preview(Instant from, Instant to) {
        return new SummarizeFigmaCommentsCommand(from, to, true, false, false);
    }
}
```

### 시간창 결정 흐름 (스케줄러)

```
1. 스케줄러 진입 → FigmaSummaryCursor 행 SELECT FOR UPDATE
2. now = Instant.now()
3. from = cursor.lastWindowEnd ?? (now - pollInterval × 2)   # 초기 부팅 시 안전 fallback
4. to   = now
5. SummarizeFigmaCommentsUseCase.summarize(scheduledSync(from, to))
6. 발송 성공한 도메인 묶음의 commentId 들에 대해 dispatch insert
7. cursor.lastWindowEnd = to (커밋)
```

### 중복 발송 방지 흐름

```
filtered = comments  in [from, to]
to_send  = filtered \ existing(figma_comment_dispatch.comment_id) when not force
to_show  = filtered                                                 # preview 는 dispatch 무관
```

발송 후 `figma_comment_dispatch` insert 는 발송 트랜잭션과 분리된 별도 트랜잭션 (`Propagation.REQUIRES_NEW`) 으로 실행한다. 한 도메인 발송 실패가 다른 도메인의 dispatch 기록을 막지 않게 한다.

### 데이터 모델 (Flyway)

기존 4개 마이그레이션 (`V10.00`, `V10.10`, `V10.20`, `V21.30`) 을 단일 `V2026.05.07.10.00__create_figma_tables.sql` 로 병합한다. 병합 결과 figma 도메인 스키마 전체가 한 파일에서 정의되며, 각 블록 위에 책임 ADR 을 주석으로 남긴다. 병합 후 `V10.10`, `V10.20`, `V21.30` 파일은 삭제한다.

```sql
-- =====================================================================================
-- Figma 도메인 통합 마이그레이션
-- ADR-003 (Figma 댓글 → Discord 포워딩) + amendment 1·2차 (LLM 분류 + 도메인 라우팅 + embed)
-- ADR-004 (시간창 단일 유즈케이스 + 중복 발송 가드)
--
-- 본 파일은 figma 도메인의 모든 테이블을 한 번에 정의한다. 본 마이그레이션 4개 (V10.00 /
-- V10.10 / V10.20 / V21.30) 가 develop/main 에 미머지 상태였으므로 deprecate / drop 단계를
-- 거치지 않고 단일 파일로 병합했다. 이후 figma 도메인 스키마 변경은 신규 V2026.MM.DD 파일을
-- 추가하는 정상 패턴으로 진행한다.
-- =====================================================================================


-- ADR-003 §Decision 1: Figma OAuth refresh/access token 보관 (운영진 1인 위임)
CREATE TABLE figma_integration
(
    id                       BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    owner_member_id          BIGINT                      NOT NULL,
    refresh_token_enc        TEXT                        NOT NULL,
    access_token_enc         TEXT,
    access_token_expires_at  TIMESTAMP(6) WITH TIME ZONE,
    scope                    VARCHAR(500)                NOT NULL,
    created_at               TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at               TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uix_figma_integration_owner ON figma_integration (owner_member_id);


-- ADR-003 §Decision 2: 폴링 대상 파일 메타데이터 (file_key, 활성 여부, 마지막 fetch 시각)
-- ADR-004 §Decision 5: 동기화 진행 boundary 였던 last_synced_comment_id 컬럼은 정의하지 않는다.
--   "어느 시각까지 봤는가" 는 figma_summary_cursor 가 담당하고,
--   "어느 댓글이 발송됐는가" 는 figma_comment_dispatch 가 담당한다.
CREATE TABLE figma_watched_file
(
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    file_key        VARCHAR(100)                NOT NULL,
    display_name    VARCHAR(255)                NOT NULL,
    enabled         BOOLEAN                     NOT NULL DEFAULT TRUE,
    last_synced_at  TIMESTAMP(6) WITH TIME ZONE,           -- 마지막으로 이 파일을 fetch 한 시각
    last_error      TEXT,                                  -- 마지막 fetch 에서 발생한 오류 (성공 시 NULL)
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uix_figma_watched_file_key      ON figma_watched_file (file_key);
CREATE INDEX        ix_figma_watched_file_enabled   ON figma_watched_file (enabled);


-- ADR-003 amendment 1차 (2026-05-07): 페이지명 매핑 대신 LLM 분류 결과(domain_key) 를
--   라우팅 키로 사용. 도메인 단위로 Discord webhook URL 1개를 묶고, 멘션 대상은 N개까지
--   영속화한다. 본 ADR 채택 시점에 폐기된 figma_part_route 는 정의하지 않는다.
CREATE TABLE figma_routing_domain
(
    id                   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    domain_key           VARCHAR(100)                NOT NULL,
    description          VARCHAR(500),
    discord_webhook_url  TEXT                        NOT NULL,
    fallback             BOOLEAN                     NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uix_figma_routing_domain_key      ON figma_routing_domain (domain_key);
CREATE INDEX        ix_figma_routing_domain_fallback  ON figma_routing_domain (fallback);


-- ADR-003 amendment 1차: 도메인별 멘션 대상 (Discord role / user) N개. embed 외부 content 영역에 출력.
CREATE TABLE figma_routing_domain_mention
(
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    domain_id       BIGINT                      NOT NULL,
    mention_id      VARCHAR(50)                 NOT NULL,
    mention_type    VARCHAR(20)                 NOT NULL,   -- ROLE | USER
    display_label   VARCHAR(255),
    created_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_frdm_domain FOREIGN KEY (domain_id) REFERENCES figma_routing_domain (id) ON DELETE CASCADE
);

CREATE INDEX        ix_figma_routing_domain_mention_domain_id ON figma_routing_domain_mention (domain_id);
CREATE UNIQUE INDEX uix_figma_routing_domain_mention          ON figma_routing_domain_mention (domain_id, mention_type, mention_id);


-- ADR-003 amendment 후속 (3-tier 분류 캐시 §L2): commentId → domain_key 영구 캐시.
--   재시작/다중 인스턴스 환경에서도 동일 commentId 의 LLM 재호출을 막는다.
--   mock provider 응답 / 후보 외 응답은 본 테이블에 저장하지 않고 in-memory L1 캐시에만 둔다.
CREATE TABLE figma_comment_classification
(
    id            BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    comment_id    VARCHAR(255)                NOT NULL,
    domain_key    VARCHAR(100)                NOT NULL,
    provider      VARCHAR(64)                 NOT NULL,
    classified_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uix_figma_comment_classification_comment_id   ON figma_comment_classification (comment_id);
CREATE INDEX        ix_figma_comment_classification_classified_at ON figma_comment_classification (classified_at);


-- ADR-004 §Decision 4: 단일 행 전역 cursor. 스케줄러는 (last_window_end, now] 시간창을 사용하며,
--   발송 성공 시 last_window_end 를 now 로 advance 한다. 다중 인스턴스 환경에서는 SELECT FOR UPDATE
--   또는 ShedLock 으로 직렬화한다. application 코드가 단일 row 불변을 보장한다.
CREATE TABLE figma_summary_cursor
(
    id                BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    last_window_end   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL
);


-- ADR-004 §Decision 3: 댓글 단위 발송 기록. 시간창 시맨틱으로 옮기면서 사라진
--   "이미 발송된 댓글을 다시 보내지 않는다" 가드 책임을 본 테이블이 담당한다.
--   admin digest 는 force=true 로 본 테이블을 무시하고 재발송할 수 있다 (catch-up 시맨틱 유지).
--   90일 보존이며 별도 회수 잡으로 정리한다 (ADR-004 §Implementation Plan §7).
CREATE TABLE figma_comment_dispatch
(
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    comment_id      VARCHAR(255)                NOT NULL,
    domain_id       BIGINT                      NOT NULL,
    dispatched_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_fcd_domain FOREIGN KEY (domain_id) REFERENCES figma_routing_domain (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uix_figma_comment_dispatch_comment_id ON figma_comment_dispatch (comment_id);
CREATE INDEX        ix_figma_comment_dispatch_dispatched_at ON figma_comment_dispatch (dispatched_at);
```

### 환경변수 (추가)

```yaml
app:
  figma:
    summary:
      dispatch-retention: P90D       # figma_comment_dispatch 보존 기간
      cursor-bootstrap-multiplier: 2 # 초기 cursor 부재 시 (now - pollInterval × N)
```

### 운영 시 주의사항

- 다중 인스턴스 환경에서 `figma_summary_cursor` 의 advance 는 ShedLock (또는 `SELECT ... FOR UPDATE`) 으로 직렬화한다. 본 ADR 시점에 ShedLock 미도입이면 `summarize-figma-comments` 잡 단독 락을 추가한다.
- digest API 는 기본 `force=true` 이므로, 운영진이 같은 시간창을 여러 번 호출하면 중복 발송이 발생한다 (의도). 이 동작은 ADR-003 2차 amendment 와 동일하다.
- preview API 는 `dryRun=true` 이므로 dispatch / cursor 갱신을 하지 않는다. 따라서 preview → "좋네" → sync 트리거 의 순서를 밟아도 sync 가 같은 댓글을 다시 분류/발송한다 (L2 캐시 덕분에 LLM 비용 추가 없음).
- figma 도메인 마이그레이션 4종 (`V10.00`, `V10.10`, `V10.20`, `V21.30`) 은 본 ADR 채택 시점에 develop/main 에 미머지 상태이므로, **단일 `V10.00` 파일로 병합** 하면서 `last_synced_comment_id` 정의 제거, `figma_part_route` 정의 누락, `figma_summary_cursor` / `figma_comment_dispatch` 신규 정의를 한꺼번에 처리한다. 운영 환경에 적용된 적이 없는 feature 브랜치 한정 변경이므로 deprecate-then-drop / 운영 안정화 기간을 두지 않는다.
- 병합 파일에는 각 테이블 블록 위에 `-- ADR-NNN §Decision M:` 주석을 남겨, git blame 없이도 의도가 추적되게 한다 (자세한 본문은 §Implementation Notes › 데이터 모델 참조).
- `figma_comment_dispatch` 는 90일 보존이므로, 90일 이상 지난 댓글이 다시 발송되지 않게 하려면 운영진이 명시적으로 force 옵션을 사용해야 한다.
- 본 ADR 은 ADR-003 의 amendment 가 아니라 후속 ADR 이다. 채택 후 `FigmaCommentDomainClassifier.java` 의 javadoc 의 `ADR-006 §Decision 5` 참조를 ADR-006 (3-tier 캐시) 의 실제 번호와 정합하도록 점검한다.

## Implementation Plan (Commit 단위)

각 커밋은 단독으로 빌드/테스트 통과해야 하며, Conventional Commits 규칙(`<type>: <subject>`)을 따른다. PR 은 의미 단위로 묶어 `[Refactor] Figma 댓글 동기화를 시간창 단일 유즈케이스로 통합` 등의 제목으로 한다.

> **실행 시 ordering 정정 (2026-05-07)**: `application.yml` / 테스트 프로필 모두 `spring.jpa.hibernate.ddl-auto: validate` 로 설정되어 있어 신규 엔티티를 먼저 추가하면 부트/통합 테스트가 깨진다. 따라서 원안의 §1 (엔티티 추가) 과 §4 (마이그레이션 병합) 의 순서를 swap 해 마이그레이션을 먼저 적용한다. 기능 산출물은 동일하지만 중간 커밋 구간의 빌드/테스트 통과를 보장한다.

> 1번부터 10번까지 순서대로 적용. 각 단계 종료 시점에 운영 관찰 가능 시간을 두는 것을 권장 (특히 6번 이후).

1. `chore: figma 마이그레이션 4종을 단일 파일로 병합하고 ADR-004 신규 테이블 추가`
   - **figma 도메인 마이그레이션 4종 병합** (모두 develop/main 미머지 feature 브랜치 한정 변경):
     - 보존 + 통합 대상: [`V2026.05.07.10.00__create_figma_tables.sql`](../../src/main/resources/db/migration/V2026.05.07.10.00__create_figma_tables.sql) — 본 파일 1개에 figma 도메인 전체 스키마를 통합.
     - 삭제 대상: `V2026.05.07.10.10__create_figma_routing_domain_tables.sql`, `V2026.05.07.10.20__drop_figma_part_route.sql`, `V2026.05.07.21.30__create_figma_comment_classification.sql` — 본 커밋에서 git rm.
   - 통합 파일에 `figma_summary_cursor`, `figma_comment_dispatch` 두 신규 테이블도 함께 정의해 이후 커밋의 엔티티 추가가 ddl-auto=validate 환경에서 부트되도록 한다.
   - `last_synced_comment_id` 컬럼은 본 커밋에서 정의를 유지한다 — 기존 sync / preview 코드가 본 컬럼을 참조하므로 진입점 위임 (4번 커밋) 이후 5번 커밋에서 제거한다.
   - 각 테이블 CREATE 블록 위에 책임 ADR/Decision 번호를 한 줄 주석으로 남김.
   - `figma_part_route` 는 amendment 1차에서 폐기되었으므로 통합본에 포함하지 않음 (V10.20 의 DROP 도 자연 소멸).

2. `feat: figma summary cursor / comment dispatch 도메인과 Port/Adapter 추가`
   - `FigmaSummaryCursor`, `FigmaCommentDispatch` 엔티티 + Builder + 도메인 메서드 (`bootstrap(initialEnd)`, `advance(Instant)`, `of(commentId, domainId, dispatchedAt)`).
   - JPA Repository, Persistence Adapter, Save/Load Port (`LoadFigmaSummaryCursorPort`, `SaveFigmaSummaryCursorPort`, `LoadFigmaCommentDispatchPort`, `SaveFigmaCommentDispatchPort`).
   - cursor 의 advance 는 `newEnd >= lastWindowEnd` 일 때만 적용해 같은 시간창의 재발송을 방어한다.
   - dispatch 어댑터는 unique 제약 race 를 조용히 흡수한다 (기존 `figma_comment_classification` 패턴 동일).
   - 본 커밋 시점에는 어떤 호출 경로도 신규 Port 를 사용하지 않는다.

3. `feat: SummarizeFigmaCommentsUseCase 와 단일 본체 service 추가`
   - 신규 `SummarizeFigmaCommentsUseCase` (in port) + `SummarizeFigmaCommentsCommand` record (정적 팩토리: scheduledSync / singleFileSync / digest / preview / previewSingleFile) + `FigmaSummaryResult` record (도메인 묶음 응답 + alreadyDispatched 정보).
   - 신규 `FigmaCommentSummaryService` 구현. 기존 `FigmaCommentBatchProcessor` 의 Mode 이중 분기를 단일 시간창 필터로 축소.
   - 분류 호출은 기존 `FigmaCommentDomainClassifier.classifyBatch` 그대로 재사용 (3-tier 캐시 변경 없음).
   - 발송 시 `figma_comment_dispatch` 에 commentId 가 있고 `force=false` 면 send 대상에서 제외. 발송 성공 후 dispatch insert + (advanceCursor=true 면) cursor advance.
   - 본 커밋 시점에는 새 진입점은 호출되지 않는다.

4. `refactor: 기존 sync / digest / preview 를 SummarizeFigmaCommentsUseCase 로 위임`
   - `FigmaCommentSyncCommandService` → 내부적으로 `SummarizeFigmaCommentsUseCase.summarize(scheduledSync(...))` 호출하는 thin shim 으로 축소. syncOne 은 singleFileSync 팩토리로 cursor 비변경 단일 파일 trace 모드로 동작.
   - `FigmaCommentDigestService` → `summarize(digest(...))` 위임 후 응답을 `FigmaDigestSummary` 로 매핑.
   - `FigmaCommentPreviewQueryService` → `summarize(previewSingleFile(...))` 위임. 시간창 기본값은 `now - pollInterval × 2 ~ now`.
   - `FigmaCommentBatchProcessor` 삭제 (Mode 분기 본체).
   - 본 커밋 시점부터 `last_synced_comment_id` 는 read 만 일어나고 write 는 일어나지 않는다.

5. `refactor: figma_watched_file.last_synced_comment_id 컬럼/엔티티 필드 제거`
   - 1번 커밋의 통합 마이그레이션에서 `last_synced_comment_id` 컬럼 라인 삭제.
   - `FigmaWatchedFile` 엔티티에서 `lastSyncedCommentId` 필드 삭제 + `markSynced(commentId, syncedAt)` → `markFetched(syncedAt)` 단순화.
   - `FigmaWatchedFileStateUpdater.advance(...)` 메서드 자체 제거. dispatch / cursor 갱신은 본체 service 가 담당하므로 불필요.
   - `FigmaCommentPreviewInfo` / `FigmaWatchedFileInfo` / `FigmaWatchedFileResponse` DTO 에서 `lastSyncedCommentId` 필드 제거.
   - 관련 javadoc / 컨트롤러 주석을 시간창 / dispatch / cursor 시맨틱으로 갱신.

6. `feat: figma 스케줄러를 시간창 + cursor 기반으로 전환`
   - `FigmaCommentSyncScheduler.poll()` 가 `figma_summary_cursor.last_window_end` 를 from 으로, `now` 를 to 로 하는 시간창으로 `summarize(scheduledSync(from, to))` 호출. cursor 부재 시 안전 fallback 으로 `now - pollInterval × 2` 사용.
   - `FigmaCommentSyncCommandService.syncAll()` 도 동일 cursor 기반 로직을 사용해 운영자 수동 트리거가 스케줄러와 시맨틱을 통일.
   - 다중 인스턴스 환경에서는 두 인스턴스가 같은 시간창을 둘 다 처리할 수 있지만, dispatch unique(comment_id) + cursor 의 방어적 advance 로 중복 발송이 차단된다. ShedLock 도입은 후속 작업.

7. `feat: figma preview API 를 시간창 시맨틱으로 노출하고 alreadyDispatched 정보 추가`
   - 신규 `FigmaPreviewController` — `GET /api/v1/admin/figma/preview?from=&to=&watchedFileId=` 로 시간창 / 단일 파일 / 전체 활성 파일 preview 를 모두 처리. 응답은 `FigmaSummaryResult` 통일 포맷.
   - 기존 `GET /api/v1/admin/figma/sync/watched-files/{id}/preview` 는 `@Deprecated` 로 유지 (호환).
   - `FigmaCommentPreviewInfo.Comment` 에 `boolean alreadyDispatched` 필드 추가.
   - `FigmaCommentSummaryService` 의 dispatch 조회를 항상 수행하도록 수정 (기존: `dryRun || force` 시 빈 Set → 응답의 alreadyDispatched 가 잘못 false 였던 버그 수정).

8. `feat: figma_comment_dispatch 회수 스케줄러 추가`
   - `FigmaSummaryProperties` (app.figma.summary.dispatch-retention=P90D, retention-poll-interval=PT24H).
   - `FigmaCommentDispatchRetentionScheduler` — 일 1회 보존 기간 초과 행을 DELETE. `figma.sync.enabled=false` 면 회수도 건너뜀.

9. `test: figma 시간창 통합 시나리오 단위 테스트 추가`
   - `FigmaSummaryCursorTest` — advance 방어 로직 (미래/과거/null/idempotent) 검증.
   - `FigmaCommentSummaryServiceTest` — 핵심 시나리오 4종: sync 첫 호출 발송+dispatch+cursor advance / sync 재호출 dedup / digest force 무시 / preview dryRun 부수효과 0.
   - Testcontainers 기반 cursor 락 경합 통합 테스트는 ShedLock 도입과 함께 후속 작업으로 분리.

10. `docs: ADR-003 amendment 3차 + ADR-004 ordering 정정 + 운영 가이드`
    - ADR-003 본문 상단에 "ADR-004 가 sync 필터 시맨틱 + 중복 발송 가드 부분만 supersede" 표기 추가.
    - ADR-004 본문 (이 문서) Implementation Plan 의 ordering 정정 반영 (ddl-auto=validate 제약).
    - `docs/guides/Figma_댓글_Discord_포워딩_보고서.md` 의 sync state / preview / digest 섹션을 시간창 시맨틱으로 갱신.
    - 운영 메뉴얼: cursor 수동 조정, dispatch 강제 삭제, 90일 보존 정책 운영 주의사항 추가.

> 본 10개 커밋이 모두 머지된 시점부터 신규 문서/PR 에서는 `last_synced_comment_id` / `figma_part_route` 표현을 사용하지 않는다. 두 정의는 1·5번 커밋 (마이그레이션 통합 + 컬럼 제거) 과정에서 자연 소멸하며, 별도 DROP 마이그레이션이나 운영 안정화 기간이 필요하지 않다 (figma 도메인 마이그레이션 4종이 모두 develop/main 미머지 상태이기 때문).

## References

- 관련 ADR
    - [ADR-003: Figma 파일 댓글을 OAuth 기반으로 폴링해 담당 파트별 Discord 멘션으로 전달한다](003-figma-comment-discord-forwarder.md) — 본 ADR 이 sync 필터/발송 가드 부분만 supersede 한다. OAuth, LLM 분류, 도메인 라우팅, embed 포맷, digest API 자체 는 그대로 유효.
- 기존 코드
    - [FigmaCommentBatchProcessor](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentBatchProcessor.java) — `Mode` 분기 제거 대상
    - [FigmaCommentDomainClassifier](../../src/main/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifier.java) — 3-tier 캐시는 그대로 유지
    - [FigmaWatchedFile](../../src/main/java/com/umc/product/figma/domain/FigmaWatchedFile.java) — `last_synced_comment_id` 의존 제거 대상
    - [FigmaCommentSyncScheduler](../../src/main/java/com/umc/product/figma/adapter/in/scheduler/FigmaCommentSyncScheduler.java) — 시간창 + cursor 로 재정의 대상
- 운영 가이드
    - [Figma 댓글 Discord 포워딩 보고서](../guides/Figma_댓글_Discord_포워딩_보고서.md) — 9번 커밋에서 시간창 시맨틱으로 갱신 예정
