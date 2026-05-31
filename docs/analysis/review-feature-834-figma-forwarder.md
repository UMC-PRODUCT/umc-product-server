# PR 리뷰 보고서: feature/#834-figma-forwarder → develop

**작성 일자:** 2026-05-08  
**대상 브랜치:** `feature/#834-figma-forwarder`  
**병합 대상:** `develop`  
**변경 규모:** 152개 파일, +12,941줄  
**관련 이슈:** resolves #834

---

## 종합 판정

```
🔴 MERGE BLOCKED — P1 이슈 3건 해소 후 재검토 필요
```

| 우선순위 | 건수 | 내용 |
|---------|------|------|
| P1 (Critical) | 3 | 보안·데이터 무결성 위반 — 배포 전 반드시 해소 |
| P2 (Significant) | 7 | 아키텍처·성능·동시성 문제 |
| P3 (Code Quality) | 6 | 테스트 누락, 코드 품질 |
| P4 (Informational) | 4 | 운영 개선 권고 |

---

## 작업 범위 검토 (Scope Check)

```
Scope Check: CLEAN
Intent:  Figma 댓글을 LLM으로 도메인 분류하고 Discord에 멘션 embed 발송
Delivered: Figma OAuth + 댓글 동기화 + LLM 분류 3-tier 캐시 + Discord 발송 + 어드민 API 전체 구현
Plan items: 모든 커밋이 intent와 일치. 불필요한 scope 없음.
```

---

## P1 — Critical (머지 차단)

### P1-1. `FigmaTokenCipher` — 하드코딩된 기본 암호화 키

**파일:** [FigmaTokenCipher.java:33](src/main/java/com/umc/product/figma/application/service/FigmaTokenCipher.java#L33)

`FIGMA_TOKEN_ENCRYPTION_KEY` 환경변수가 설정되지 않으면 모든 Figma OAuth 토큰이 소스코드에 노출된 고정 문자열 `"umc-product-figma-default-key"`로 암호화된다. DB 읽기 권한을 가진 공격자는 전 토큰을 즉시 복호화할 수 있다.

```java
// 현재
if (rawKey == null || rawKey.isBlank()) {
    rawKey = "umc-product-figma-default-key";  // ← 위험
}

// 수정
if (rawKey == null || rawKey.isBlank()) {
    throw new FigmaDomainException(FigmaErrorCode.TOKEN_ENCRYPTION_FAILED,
        "FIGMA_TOKEN_ENCRYPTION_KEY가 설정되지 않았습니다.");
}
```

`application.yml`의 `token-encryption-key: ${FIGMA_TOKEN_ENCRYPTION_KEY:umc-product-figma-default-key}` 기본값도 함께 제거해야 한다.

---

### P1-2. `figma_comment_dispatch` — `ON DELETE CASCADE`가 중복 발송 방지 불변식을 파괴

**파일:** [V2026.05.07.10.00__create_figma_tables.sql:121](src/main/resources/db/migration/V2026.05.07.10.00__create_figma_tables.sql#L121)

ADR-004 §Decision 3에서 `figma_comment_dispatch`가 "이미 발송된 댓글을 다시 보내지 않는다"는 유일한 가드임을 명시한다. 그러나 현재 `domain_id`의 FK 제약이 `ON DELETE CASCADE`이므로, 도메인 삭제 → 재생성 시 발송 이력이 사라져 동일 댓글이 재발송된다.

```sql
-- 현재
CONSTRAINT fk_fcd_domain FOREIGN KEY (domain_id)
    REFERENCES figma_routing_domain (id) ON DELETE CASCADE  -- ← 위험

-- 수정
CONSTRAINT fk_fcd_domain FOREIGN KEY (domain_id)
    REFERENCES figma_routing_domain (id) ON DELETE RESTRICT
```

도메인 삭제가 필요한 경우 application 레이어에서 dispatch 이력을 먼저 아카이브하는 절차가 필요하다.

---

### P1-3. `DiscordMentionWebhookAdapter.sendPages()` — 부분 발송 후 실패 시 dispatch 기록 누락

**파일:** [DiscordMentionWebhookAdapter.java:474](src/main/java/com/umc/product/figma/adapter/out/external/DiscordMentionWebhookAdapter.java#L474), [FigmaCommentSummaryService.java:168](src/main/java/com/umc/product/figma/application/service/FigmaCommentSummaryService.java#L168)

페이지 1 Discord 발송 성공 → 페이지 2 실패 시, `sendDomainBatch`는 `false`를 반환하고 호출자는 `recordDispatched`를 건너뛴다. Discord에는 이미 메시지가 발송됐지만 dispatch 기록이 없으므로 다음 사이클에 동일 댓글이 재발송된다.

```java
// FigmaCommentSummaryService.java
// 현재: sent == false면 전체 recordDispatched 스킵
if (sent) {
    saveFigmaCommentDispatchPort.recordDispatched(...);
}

// 수정: sendDomainBatch가 실제 발송된 commentId 목록을 반환하도록 시그니처 변경
List<String> dispatchedIds = sendDomainBatch(domain, sendable, ...);
if (!dispatchedIds.isEmpty()) {
    saveFigmaCommentDispatchPort.recordDispatched(dispatchedIds, domain.getId(), Instant.now());
}
```

---

## P2 — Significant (develop 머지 전 해소 권고)

### P2-1. 헥사고날 아키텍처 위반 — `adapter/in`이 `port/out` 또는 `adapter/out` 직접 주입

세 곳에서 의존 방향 규칙 위반이 발견됐다:

| 파일 | 위반 내용 |
|------|----------|
| [FigmaOAuthController.java:29](src/main/java/com/umc/product/figma/adapter/in/web/FigmaOAuthController.java#L29) | `adapter/in`이 `application/port/out`인 `FigmaOAuthPort` 직접 주입 |
| [FigmaSyncController.java:50](src/main/java/com/umc/product/figma/adapter/in/web/FigmaSyncController.java#L50) | `adapter/in`이 `adapter/out/external`인 `FigmaSyncProperties` 직접 주입 |
| [FigmaCommentSummaryService.java:61](src/main/java/com/umc/product/figma/application/service/FigmaCommentSummaryService.java#L61) | `application/service`가 `adapter/out/external`인 `FigmaSyncProperties` 직접 주입 |
| [FigmaCommentSyncCommandService.java:30](src/main/java/com/umc/product/figma/application/service/FigmaCommentSyncCommandService.java#L30) | 동일하게 `FigmaSyncProperties` 직접 주입 |

**수정 방향:**
- `buildAuthorizeUrl`을 `RegisterFigmaIntegrationUseCase`에 포함하거나 별도 Query UseCase로 이동
- `FigmaSyncProperties.pollInterval()` / `maxFilesPerRun()`이 필요한 경우 `SummarizeFigmaCommentsCommand`에 파라미터로 전달하거나 `application/port/out`에 설정 인터페이스 정의

---

### P2-2. `FigmaCommentSummaryService.summarize()` — `@Transactional` 누락

**파일:** [FigmaCommentSummaryService.java:64](src/main/java/com/umc/product/figma/application/service/FigmaCommentSummaryService.java#L64)

`recordDispatched`, `save(cursor)`, `markIdle`/`recordError` 등 여러 쓰기 작업이 단일 트랜잭션 보장 없이 실행된다. 중간 실패 시 dispatch 기록과 cursor 상태가 불일치할 수 있다.

```java
@Transactional
public FigmaSummaryResult summarize(SummarizeFigmaCommentsCommand command) { ... }
```

단, `resolveActiveAccessToken`이 `REQUIRES_NEW`를 사용하므로 token refresh는 외부 트랜잭션과 분리됨을 확인 후 적용.

---

### P2-3. `resolveActiveAccessToken()` — 동시 토큰 갱신 레이스 컨디션

**파일:** [FigmaIntegrationCommandService.java:71](src/main/java/com/umc/product/figma/application/service/FigmaIntegrationCommandService.java#L71)

두 스레드가 동시에 만료된 토큰을 감지하고 둘 다 Figma refresh 엔드포인트를 호출할 수 있다. Figma의 refresh token이 one-time-use이면 두 번째 호출이 실패하고 토큰이 폐기된다.

```java
// LoadFigmaIntegrationPort 구현에 비관적 잠금 추가
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<FigmaIntegration> findActive();
```

---

### P2-4. `FigmaCommentDomainClassifier.classifyBatch()` — LLM 할루시네이션 commentId가 DB에 영구 캐시

**파일:** [FigmaCommentDomainClassifier.java:282](src/main/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifier.java#L282)

`parseBatchResponse`는 LLM 응답 JSON의 `commentId`가 입력 배치에 실제로 존재하는지 검증하지 않는다. 할루시네이션으로 생성된 가짜 commentId가 `figma_comment_classification` 테이블에 저장되면, 이후 Figma에서 동일 ID의 실제 댓글이 생성될 경우 잘못된 도메인으로 영구 라우팅된다.

```java
// parseBatchResponse에 추가
Set<String> inputIds = comments.stream()
    .map(FigmaCommentInfo::commentId)
    .collect(Collectors.toSet());
// ...
if (!inputIds.contains(commentId)) {
    log.warn("LLM 응답에 입력에 없는 commentId 포함: {}", commentId);
    continue;
}
```

---

### P2-5. N+1 쿼리 — `recordDispatched`, 도메인별 mentions 조회

**파일:** [FigmaCommentDispatchPersistenceAdapter.java:45](src/main/java/com/umc/product/figma/adapter/out/persistence/FigmaCommentDispatchPersistenceAdapter.java#L45), [FigmaCommentSummaryService.java:228](src/main/java/com/umc/product/figma/application/service/FigmaCommentSummaryService.java#L228)

| 위치 | 문제 | 영향 |
|------|------|------|
| `recordDispatched` | commentId 개수만큼 `existsBy` + `save` 순차 호출 (O(2n) DB 왕복) | 100개 댓글 → 200 DB 쿼리 |
| `sendDomainBatch` + `buildDomainGroup` | 같은 도메인의 mentions를 루프 안에서 각각 개별 조회 (도메인당 2회) | N 도메인 → 2N SELECT |

**수정:**
- `recordDispatched`: `saveAll()` + PostgreSQL `ON CONFLICT DO NOTHING` upsert로 전환
- mentions: 루프 진입 전 `listMentionsByDomainIdIn(domainIds)`로 일괄 조회 후 Map으로 분배

---

### P2-6. `FigmaCommentClient` — Figma API 페이지네이션 없음

**파일:** [FigmaCommentClient.java:60](src/main/java/com/umc/product/figma/adapter/out/external/FigmaCommentClient.java#L60)

댓글이 수천 개 이상인 파일에서 단일 REST 호출로 전체를 가져온다. 대용량 응답으로 메모리 압박 및 타임아웃이 발생할 수 있다. Figma API의 cursor/pagination 파라미터 도입 필요.

---

### P2-7. `FigmaCommentDomainClassifier` — LLM batch 크기 상한 없음

**파일:** [FigmaCommentDomainClassifier.java:180](src/main/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifier.java#L180)

캐시 미스된 댓글 전량을 단일 LLM 호출에 전달한다. 댓글 수가 많으면 provider 컨텍스트 한도를 초과해 전체 배치가 실패한다.

```java
// 최대 배치 크기 상수 추가 후 청크 분할
private static final int MAX_BATCH_SIZE = 30;
// Lists.partition(uncached, MAX_BATCH_SIZE)로 분할 처리
```

---

## P3 — Code Quality (테스트 누락)

핵심 보안 컴포넌트에 테스트가 없다.

| 파일 | 누락된 테스트 |
|------|-------------|
| [FigmaTokenCipher.java](src/main/java/com/umc/product/figma/application/service/FigmaTokenCipher.java) | encrypt→decrypt 왕복, 변조된 암호문 복호화 시 예외 |
| [FigmaOAuthStateStore.java](src/main/java/com/umc/product/figma/application/service/FigmaOAuthStateStore.java) | TTL 만료 거부, replay 차단(동일 state 재사용), null state |
| [FigmaOAuthController.java:51](src/main/java/com/umc/product/figma/adapter/in/web/FigmaOAuthController.java#L51) | `@Public` callback — 만료 state, 미등록 state, replay 공격 |
| [FigmaCommentSummaryService.java:86](src/main/java/com/umc/product/figma/application/service/FigmaCommentSummaryService.java#L86) | `listAllDomains()` 빈 리스트 → `ROUTING_DOMAIN_NOT_REGISTERED` 예외 |
| [LlmCallGuard.java:59](src/main/java/com/umc/product/llm/application/service/LlmCallGuard.java#L59) | 차단 해제 후 재차단 시나리오 (half-open → open 재전환) |

---

## P4 — Informational (운영 개선 권고)

### P4-1. `figma_summary_cursor` — 단일 행 불변식을 DB 레벨로 강제하지 않음

**파일:** [V2026.05.07.10.00__create_figma_tables.sql:103](src/main/resources/db/migration/V2026.05.07.10.00__create_figma_tables.sql#L103)

다중 인스턴스 bootstrap 시 cursor 행이 2개 생성될 수 있다. `CHECK (id = 1)` 또는 singleton 컬럼 패턴으로 DB 레벨 강제 권고.

### P4-2. Discord webhook URL 도메인 검증 없음

**파일:** [RegisterFigmaRoutingDomainRequest.java](src/main/java/com/umc/product/figma/adapter/in/web/dto/request/RegisterFigmaRoutingDomainRequest.java)

SUPER_ADMIN 전용이지만, `discordWebhookUrl`에 내부 네트워크 주소를 등록할 수 있다 (SSRF 벡터). `@Pattern` 어노테이션으로 `discord.com/api/webhooks/` 형식을 강제 권고.

### P4-3. LLM 프롬프트 인젝션 — 댓글 본문 비검증 삽입

**파일:** [FigmaCommentDomainClassifier.java:252](src/main/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifier.java#L252)

악의적인 Figma 댓글 작성자가 프롬프트 인젝션으로 분류 결과를 조작할 수 있다. 분류 결과 외 값은 fallback 처리되므로 데이터 유출 위험은 없으나, 댓글 본문 최대 길이(예: 2000자) 제한과 user/system 컨텍스트 명확한 분리 권고.

### P4-4. FIGMA_TOKEN_ENCRYPTION_KEY 교체 절차 부재

키를 교체하면 기존 DB 저장 토큰이 전부 복호화 불가가 된다. 키 교체 시 재암호화 마이그레이션 절차가 없다. 운영 문서에 키 교체 절차 추가 권고.

---

## 잘된 점

- AES-GCM + SecureRandom IV 조합: 대칭키 암호화 구현 자체는 올바르다.
- `FigmaOAuthStateStore`: 256-bit SecureRandom, 10분 TTL, atomic `remove()` replay 차단 — 보안 설계가 탄탄하다.
- 3-tier 캐시(L1 Caffeine + L2 DB + L3 LLM) 전략이 ADR-006에 명확히 문서화되어 있고 negative cache poisoning도 고려됐다.
- Discord 6000바이트 합산 한도 처리(`ensureWithinDiscordLimit`, dynamic packing)가 꼼꼼하다.
- 테스트가 12개 파일에 걸쳐 존재하고, Given/When/Then + 한국어 `@DisplayName`을 잘 준수하고 있다.
- 아키텍처 문서(ADR 003~012) 충실도가 매우 높다.

---

## PR Quality Score

```
quality_score = 10 − (3 × 2 + 7 × 0.5) = 10 − 9.5 = 0.5/10
→ P1 이슈 해소 후 재산정 예상: ~6/10
```

---

## 머지 체크리스트

- [ ] **P1-1**: `FigmaTokenCipher` — 기본 키 fallback 제거, env var 부재 시 fail-fast
- [ ] **P1-2**: `V2026.05.07.10.00__create_figma_tables.sql` — `figma_comment_dispatch.domain_id` FK를 `ON DELETE RESTRICT`로 변경 (신규 마이그레이션 필요)
- [ ] **P1-3**: `sendDomainBatch` 시그니처 변경 → 부분 발송 시에도 성공한 commentId dispatch 기록
- [ ] **P2-1**: 아키텍처 위반 4건 해소 (FigmaOAuthController, FigmaSyncController, FigmaCommentSummaryService, FigmaCommentSyncCommandService)
- [ ] **P2-2**: `FigmaCommentSummaryService.summarize()` `@Transactional` 추가
- [ ] **P2-4**: `parseBatchResponse` — 입력 배치에 없는 commentId 필터링
- [ ] **P3**: `FigmaTokenCipher`, `FigmaOAuthStateStore`, callback 부정 경로 테스트 추가
- [ ] (optional) **P2-3**: `resolveActiveAccessToken` PESSIMISTIC_WRITE 잠금
- [ ] (optional) **P2-5**: N+1 해소 (batch insert, mentions 일괄 조회)
- [ ] (optional) **P4-2**: Discord webhook URL `@Pattern` 검증 추가
