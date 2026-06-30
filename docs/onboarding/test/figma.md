# Figma 테스트 케이스

- 테스트 파일: 10개
- 테스트 케이스: 50개
- 분류 기준: `Controller`, `UseCase`, `Repository`, `E2E`, `Scheduler`, `Domain`, `External Adapter`, `Support`

| 카테고리 | 케이스 수 |
|---|---:|
| Controller / Inbound Adapter | 4 |
| UseCase / Application Service | 33 |
| Domain | 5 |
| External Adapter | 8 |

## Controller / Inbound Adapter

### DiscordWebhookUrlMaskerTest
- 테스트 설명: DiscordWebhookUrlMasker
- 위치: `src/test/java/com/umc/product/figma/adapter/in/web/support/DiscordWebhookUrlMaskerTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [6](../../../src/test/java/com/umc/product/figma/adapter/in/web/support/DiscordWebhookUrlMaskerTest.java#L6) | 정상_webhook_URL_은_id_와_token_의_마지막_4자만_남기고_가린다 | 조건 정상_webhook_URL_은_id_와_token_의_마지막_4자만_남기고_가린다 | 성공: 검증 assertThat(masked).isEqualTo("https://discord.com/api/webhooks/****5678/****1234"); |
| [19](../../../src/test/java/com/umc/product/figma/adapter/in/web/support/DiscordWebhookUrlMaskerTest.java#L19) | DiscordWebhookUrlMasker / null_또는_빈_문자열은_그대로_반환한다 | 조건 DiscordWebhookUrlMasker / null_또는_빈_문자열은_그대로_반환한다 | 성공: 검증 assertThat(DiscordWebhookUrlMasker.mask(null)).isNull(); assertThat(DiscordWebhookUrlMasker.mask("")).isEmpty(); assertThat(DiscordWebhookUrlMasker.mask(" ")).isEqualTo(" "); |
| [27](../../../src/test/java/com/umc/product/figma/adapter/in/web/support/DiscordWebhookUrlMaskerTest.java#L27) | DiscordWebhookUrlMasker / token_이_4자_이하면_별표만_노출하고_평문_조각을_남기지_않는다 | 조건 DiscordWebhookUrlMasker / token_이_4자_이하면_별표만_노출하고_평문_조각을_남기지_않는다 | 성공: 검증 assertThat(masked).isEqualTo("https://discord.com/api/webhooks/****/****"); |
| [37](../../../src/test/java/com/umc/product/figma/adapter/in/web/support/DiscordWebhookUrlMaskerTest.java#L37) | DiscordWebhookUrlMasker / URL_에_슬래시가_없는_비정상_입력은_전부_가린다 | 조건 DiscordWebhookUrlMasker / URL_에_슬래시가_없는_비정상_입력은_전부_가린다 | 성공: 검증 assertThat(DiscordWebhookUrlMasker.mask("nothing-to-see-here")).isEqualTo("****"); |

## UseCase / Application Service

### FigmaCommentDomainClassifierTest
- 테스트 설명: FigmaCommentDomainClassifier
- 위치: `src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [64](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L64) | FigmaCommentDomainClassifier / 동일 commentId 두 번째 호출은 LLM 을 다시 부르지 않고 캐시 결과를 반환한다 | 조건 FigmaCommentDomainClassifier / 동일 commentId 두 번째 호출은 LLM 을 다시 부르지 않고 캐시 결과를 반환한다 | 성공: 검증 assertThat(first).isEqualTo("auth"); assertThat(second).isEqualTo("auth"); |
| [80](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L80) | FigmaCommentDomainClassifier / LLM 응답이 후보 외 값이면 null 을 반환하고 null 도 캐시한다 | 조건 FigmaCommentDomainClassifier / LLM 응답이 후보 외 값이면 null 을 반환하고 null 도 캐시한다 | 성공: 검증 assertThat(first).isNull(); assertThat(second).isNull(); |
| [96](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L96) | FigmaCommentDomainClassifier / LLM 호출 자체가 실패하면 null 을 반환하지만 negative 캐시는 박지 않아 다음 호출이 LLM 을 재시도한다 | 조건 FigmaCommentDomainClassifier / LLM 호출 자체가 실패하면 null 을 반환하지만 negative 캐시는 박지 않아 다음 호출이 LLM 을 재시도한다 | 실패: 에러코드 LlmErrorCode.CHAT_COMPLETION_FAILED; 검증 assertThat(first).isNull(); assertThat(second).isNull(); |
| [112](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L112) | classifyBatch 도 LLM 호출 자체 실패면 negative 캐시를 박지 않고 다음 호출이 재시도된다 | 조건 classifyBatch 도 LLM 호출 자체 실패면 negative 캐시를 박지 않고 다음 호출이 재시도된다 | 실패: 에러코드 LlmErrorCode.CHAT_COMPLETION_FAILED; 검증 assertThat(first).isEmpty(); assertThat(second).isEmpty(); |
| [129](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L129) | 후보 도메인 키가 비어 있으면 LLM 을 호출하지 않고 즉시 null 을 반환한다 | 조건 후보 도메인 키가 비어 있으면 LLM 을 호출하지 않고 즉시 null 을 반환한다 | 성공: 검증 assertThat(classifier.classify(comment, List.of())).isNull(); assertThat(classifier.classify(comment, null)).isNull(); |
| [140](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L140) | classifyBatch 는 댓글 N개를 단일 LLM 호출로 처리하고 JSON 배열 응답을 파싱한다 | 조건 classifyBatch 는 댓글 N개를 단일 LLM 호출로 처리하고 JSON 배열 응답을 파싱한다 | 성공: 검증 assertThat(results); .containsEntry("c-1", "auth"); .containsEntry("c-2", "challenger"); .containsEntry("c-3", "figma"); |
| [168](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L168) | classifyBatch 는 캐시된 댓글은 LLM 호출에서 제외하고 미캐시 댓글만 묶어서 호출한다 | 조건 classifyBatch 는 캐시된 댓글은 LLM 호출에서 제외하고 미캐시 댓글만 묶어서 호출한다 | 성공: 검증 assertThat(secondCall.userPrompt()).contains("c-fresh-1").contains("c-fresh-2"); assertThat(secondCall.userPrompt()).doesNotContain("c-cached"); assertThat(second); .containsEntry("c-cached", "auth") |
| [197](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L197) | classifyBatch 응답이 JSON 코드 블록(```json) 으로 감싸져 있어도 파싱한다 | 조건 classifyBatch 응답이 JSON 코드 블록(```json) 으로 감싸져 있어도 파싱한다 | 성공: 검증 assertThat(results).containsEntry("c-md", "auth"); |
| [215](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L215) | classifyBatch 응답에서 후보 외 domainKey 항목은 결과에 포함되지 않는다 | HTTP GET c-ok | 성공: 검증 assertThat(results).containsOnlyKeys("c-ok"); assertThat(results.get("c-ok")).isEqualTo("auth"); |
| [233](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L233) | DB 영구 캐시 히트면 LLM 을 호출하지 않고 그 값을 반환한다 | 조건 DB 영구 캐시 히트면 LLM 을 호출하지 않고 그 값을 반환한다 | 성공: 검증 assertThat(first).isEqualTo("challenger"); assertThat(second).isEqualTo("challenger"); |
| [249](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L249) | classifyBatch 에서 일부 댓글이 DB 영구 캐시에 있으면 그것만 제외하고 LLM 에 보낸다 | 조건 classifyBatch 에서 일부 댓글이 DB 영구 캐시에 있으면 그것만 제외하고 LLM 에 보낸다 | 성공: 검증 assertThat(captor.getValue().userPrompt()); .contains("c-fresh"); assertThat(results); .containsEntry("c-persisted", "auth") |
| [274](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L274) | 정상 분류는 영구 캐시에 저장된다 (실 provider) | 조건 정상 분류는 영구 캐시에 저장된다 (실 provider) | 성공: 정상 분류는 영구 캐시에 저장된다 (실 provider) |
| [287](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L287) | mock provider 응답은 영구 캐시에 저장하지 않는다 (검증 단계 임시 분류 누적 방지) | 조건 mock provider 응답은 영구 캐시에 저장하지 않는다 (검증 단계 임시 분류 누적 방지) | 성공: mock provider 응답은 영구 캐시에 저장하지 않는다 (검증 단계 임시 분류 누적 방지) |
| [300](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L300) | classify 단일 호출은 LLM 도메인에 분류 모드를 노출하지 않는다 (freeForm 형태로 전달) | 조건 classify 단일 호출은 LLM 도메인에 분류 모드를 노출하지 않는다 (freeForm 형태로 전달) | 성공: 검증 assertThat(sent.systemPrompt()).contains("후보 도메인 키").contains("정확히 하나만"); assertThat(sent.userPrompt()).contains("auth, challenger, figma"); |
| [319](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentDomainClassifierTest.java#L319) | 후보 외 응답은 영구 캐시에 저장하지 않는다 (운영 시점 후보 변경에 안전) | 조건 후보 외 응답은 영구 캐시에 저장하지 않는다 (운영 시점 후보 변경에 안전) | 성공: 후보 외 응답은 영구 캐시에 저장하지 않는다 (운영 시점 후보 변경에 안전) |

### FigmaCommentSummaryServiceTest
- 테스트 설명: FigmaCommentSummaryService
- 위치: `src/test/java/com/umc/product/figma/application/service/FigmaCommentSummaryServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [116](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentSummaryServiceTest.java#L116) | sync 첫 호출에서는 dispatch 가 비어 있어 댓글 1건이 발송되고 dispatch 에 기록된다 | 조건 sync 첫 호출에서는 dispatch 가 비어 있어 댓글 1건이 발송되고 dispatch 에 기록된다 | 성공: 검증 assertThat(result.totalComments()).isEqualTo(1); assertThat(result.skippedAlreadyDispatchedCount()).isZero(); assertThat(result.domains()).hasSize(1); assertThat(result.domains().get(0).sent()).isTrue(); |
| [139](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentSummaryServiceTest.java#L139) | sync 두 번째 호출은 dispatch 행이 있어 발송이 건너뛰어지고 skippedAlreadyDispatched 가 증가한다 | 조건 sync 두 번째 호출은 dispatch 행이 있어 발송이 건너뛰어지고 skippedAlreadyDispatched 가 증가한다 | 성공: 검증 assertThat(result.totalComments()).isEqualTo(1); assertThat(result.skippedAlreadyDispatchedCount()).isEqualTo(1); assertThat(result.domains().get(0).sent()).isFalse(); assertThat(result.domains().get(0).comments().get... |
| [161](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentSummaryServiceTest.java#L161) | digest force 모드에서는 dispatch 행이 있어도 재발송되고 skipped 는 0 으로 유지된다 | 조건 digest force 모드에서는 dispatch 행이 있어도 재발송되고 skipped 는 0 으로 유지된다 | 성공: 검증 assertThat(result.skippedAlreadyDispatchedCount()).isZero(); assertThat(result.domains().get(0).sent()).isTrue(); assertThat(result.domains().get(0).comments().get(0).alreadyDispatched()).isTrue(); |
| [184](../../../src/test/java/com/umc/product/figma/application/service/FigmaCommentSummaryServiceTest.java#L184) | preview dryRun 은 발송/dispatch 기록/cursor advance 를 모두 수행하지 않는다 | 조건 preview dryRun 은 발송/dispatch 기록/cursor advance 를 모두 수행하지 않는다 | 성공: 검증 assertThat(result.totalComments()).isEqualTo(1); assertThat(result.domains().get(0).sent()).isFalse(); assertThat(result.domains().get(0).comments().get(0).alreadyDispatched()).isTrue(); |

### FigmaPermissionEvaluatorTest
- 테스트 설명: FigmaPermissionEvaluator
- 위치: `src/test/java/com/umc/product/figma/application/service/evaluator/FigmaPermissionEvaluatorTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [14](../../../src/test/java/com/umc/product/figma/application/service/evaluator/FigmaPermissionEvaluatorTest.java#L14) | supportedResourceType은 FIGMA를 반환한다 | 호출 supportedResourceType()).isEqualTo(ResourceType.FIGMA) | 성공: 검증 assertThat(sut.supportedResourceType()).isEqualTo(ResourceType.FIGMA); |
| [25](../../../src/test/java/com/umc/product/figma/application/service/evaluator/FigmaPermissionEvaluatorTest.java#L25) | FigmaPermissionEvaluator / SUPER_ADMIN 은 READ 권한을 통과한다 | 호출 evaluate(subject, permission)).isTrue() | 성공: 검증 assertThat(sut.evaluate(subject, permission)).isTrue(); |
| [34](../../../src/test/java/com/umc/product/figma/application/service/evaluator/FigmaPermissionEvaluatorTest.java#L34) | FigmaPermissionEvaluator / SUPER_ADMIN 은 MANAGE 권한을 통과한다 | 호출 evaluate(subject, permission)).isTrue() | 성공: 검증 assertThat(sut.evaluate(subject, permission)).isTrue(); |
| [43](../../../src/test/java/com/umc/product/figma/application/service/evaluator/FigmaPermissionEvaluatorTest.java#L43) | FigmaPermissionEvaluator / 중앙운영사무국 총괄(CENTRAL_PRESIDENT) 도 READ 권한을 거부한다 | 호출 evaluate(subject, permission)).isFalse() | 실패: 검증 assertThat(sut.evaluate(subject, permission)).isFalse(); |
| [52](../../../src/test/java/com/umc/product/figma/application/service/evaluator/FigmaPermissionEvaluatorTest.java#L52) | FigmaPermissionEvaluator / 학교 회장(SCHOOL_PRESIDENT) 은 MANAGE 권한을 거부한다 | 호출 evaluate(subject, permission)).isFalse() | 실패: 검증 assertThat(sut.evaluate(subject, permission)).isFalse(); |
| [61](../../../src/test/java/com/umc/product/figma/application/service/evaluator/FigmaPermissionEvaluatorTest.java#L61) | FigmaPermissionEvaluator / 어떤 역할도 없는 사용자는 READ 권한을 거부한다 | 호출 evaluate(subject, permission)).isFalse() | 실패: 검증 assertThat(sut.evaluate(subject, permission)).isFalse(); |
| [70](../../../src/test/java/com/umc/product/figma/application/service/evaluator/FigmaPermissionEvaluatorTest.java#L70) | FigmaPermissionEvaluator / 여러 역할을 가진 사용자 중 하나라도 SUPER_ADMIN 이면 통과한다 | 호출 evaluate(subject, permission)).isTrue() | 성공: 검증 assertThat(sut.evaluate(subject, permission)).isTrue(); |

### FigmaRoutingDomainQueryServiceTest
- 테스트 설명: FigmaRoutingDomainQueryService
- 위치: `src/test/java/com/umc/product/figma/application/service/FigmaRoutingDomainQueryServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [24](../../../src/test/java/com/umc/product/figma/application/service/FigmaRoutingDomainQueryServiceTest.java#L24) | getDomainById_미존재면_ROUTING_DOMAIN_NOT_FOUND_예외 | 호출 getDomainById(99L)) | 실패: 예외 FigmaDomainException; 에러코드 FigmaErrorCode.ROUTING_DOMAIN_NOT_FOUND; 검증 .isEqualTo(FigmaErrorCode.ROUTING_DOMAIN_NOT_FOUND); |
| [45](../../../src/test/java/com/umc/product/figma/application/service/FigmaRoutingDomainQueryServiceTest.java#L45) | FigmaRoutingDomainQueryService / getDomainById_단건_조회_시_mentions_까지_채워서_반환한다 | 호출 getDomainById(10L) | 성공: 검증 assertThat(info.id()).isEqualTo(10L); assertThat(info.domainKey()).isEqualTo("auth"); assertThat(info.mentionCount()).isEqualTo(1); assertThat(info.mentions()) |
| [64](../../../src/test/java/com/umc/product/figma/application/service/FigmaRoutingDomainQueryServiceTest.java#L64) | FigmaRoutingDomainQueryService / listDomains_는_mentions_본문_없이_mentionCount_만_채운다 | 호출 listDomains() | 성공: 검증 assertThat(result).hasSize(2); assertThat(result.get(0).mentions()).isNull(); assertThat(result.get(0).mentionCount()).isEqualTo(2); assertThat(result.get(1).mentionCount()).isZero(); |
| [83](../../../src/test/java/com/umc/product/figma/application/service/FigmaRoutingDomainQueryServiceTest.java#L83) | listMentionsByDomainId_는_도메인_미존재_시_예외_정상이면_mention_리스트_반환 | 호출 listMentionsByDomainId(404L)); 호출 listMentionsByDomainId(10L) | 실패: 예외 FigmaDomainException; 검증 assertThat(mentions).singleElement(); .isEqualTo(DiscordMentionType.ROLE); |

### FigmaWatchedFileQueryServiceTest
- 테스트 설명: FigmaWatchedFileQueryService
- 위치: `src/test/java/com/umc/product/figma/application/service/FigmaWatchedFileQueryServiceTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [21](../../../src/test/java/com/umc/product/figma/application/service/FigmaWatchedFileQueryServiceTest.java#L21) | getById_미존재면_WATCHED_FILE_NOT_FOUND_예외 | 호출 getById(99L)) | 실패: 예외 FigmaDomainException; 에러코드 FigmaErrorCode.WATCHED_FILE_NOT_FOUND; 검증 .isEqualTo(FigmaErrorCode.WATCHED_FILE_NOT_FOUND); |
| [42](../../../src/test/java/com/umc/product/figma/application/service/FigmaWatchedFileQueryServiceTest.java#L42) | FigmaWatchedFileQueryService / getById_정상이면_엔티티의_sync_상태_필드까지_그대로_매핑된다 | 호출 getById(7L) | 성공: 검증 assertThat(info.id()).isEqualTo(7L); assertThat(info.fileKey()).isEqualTo("abcdef"); assertThat(info.displayName()).isEqualTo("디자인 시스템"); assertThat(info.enabled()).isTrue(); |
| [56](../../../src/test/java/com/umc/product/figma/application/service/FigmaWatchedFileQueryServiceTest.java#L56) | FigmaWatchedFileQueryService / listAll_은_enabledFilter_를_그대로_outbound_port_에_위임한다 | 호출 listAll(null)).hasSize(2); 호출 listAll(true)).singleElement(); 호출 listAll(false)).isEmpty() | 성공: 검증 assertThat(figmaWatchedFileQueryService.listAll(null)).hasSize(2); assertThat(figmaWatchedFileQueryService.listAll(true)).singleElement(); assertThat(figmaWatchedFileQueryService.listAll(false)).isEmpty(); |

## Domain

### FigmaSummaryCursorTest
- 테스트 설명: FigmaSummaryCursor
- 위치: `src/test/java/com/umc/product/figma/domain/FigmaSummaryCursorTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [7](../../../src/test/java/com/umc/product/figma/domain/FigmaSummaryCursorTest.java#L7) | bootstrap 한 cursor 의 lastWindowEnd 가 입력 시각과 일치한다 | 조건 bootstrap 한 cursor 의 lastWindowEnd 가 입력 시각과 일치한다 | 성공: 검증 assertThat(cursor.getLastWindowEnd()).isEqualTo(initial); |
| [18](../../../src/test/java/com/umc/product/figma/domain/FigmaSummaryCursorTest.java#L18) | FigmaSummaryCursor / advance 가 미래 시각이면 lastWindowEnd 가 갱신된다 | 조건 FigmaSummaryCursor / advance 가 미래 시각이면 lastWindowEnd 가 갱신된다 | 성공: 검증 assertThat(cursor.getLastWindowEnd()).isEqualTo(later); |
| [29](../../../src/test/java/com/umc/product/figma/domain/FigmaSummaryCursorTest.java#L29) | FigmaSummaryCursor / advance 가 과거 시각이면 거절되고 lastWindowEnd 는 그대로다 — 같은 시간창 재발송 방지 | 조건 FigmaSummaryCursor / advance 가 과거 시각이면 거절되고 lastWindowEnd 는 그대로다 — 같은 시간창 재발송 방지 | 성공: 검증 assertThat(cursor.getLastWindowEnd()).isEqualTo(initial); |
| [41](../../../src/test/java/com/umc/product/figma/domain/FigmaSummaryCursorTest.java#L41) | FigmaSummaryCursor / advance 가 같은 시각이면 idempotent 하게 그대로 둔다 | 조건 FigmaSummaryCursor / advance 가 같은 시각이면 idempotent 하게 그대로 둔다 | 성공: 검증 assertThat(cursor.getLastWindowEnd()).isEqualTo(initial); |
| [52](../../../src/test/java/com/umc/product/figma/domain/FigmaSummaryCursorTest.java#L52) | FigmaSummaryCursor / advance 에 null 을 넘기면 lastWindowEnd 가 변경되지 않는다 | 조건 FigmaSummaryCursor / advance 에 null 을 넘기면 lastWindowEnd 가 변경되지 않는다 | 성공: 검증 assertThat(cursor.getLastWindowEnd()).isEqualTo(initial); |

## External Adapter

### ClassificationCacheValueTest
- 테스트 설명: ClassificationCacheValue
- 위치: `src/test/java/com/umc/product/figma/adapter/out/cache/ClassificationCacheValueTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [7](../../../src/test/java/com/umc/product/figma/adapter/out/cache/ClassificationCacheValueTest.java#L7) | positive cache 값은 domainKey를 Optional로 복원한다 | 조건 positive cache 값은 domainKey를 Optional로 복원한다 | 성공: 검증 assertThat(value.classified()).isTrue(); assertThat(value.toOptional()).contains("auth"); |
| [19](../../../src/test/java/com/umc/product/figma/adapter/out/cache/ClassificationCacheValueTest.java#L19) | ClassificationCacheValue / negative cache 값은 hit 상태를 유지하되 Optional.empty로 복원한다 | 조건 ClassificationCacheValue / negative cache 값은 hit 상태를 유지하되 Optional.empty로 복원한다 | 성공: 검증 assertThat(value.classified()).isFalse(); assertThat(value.toOptional()).isEmpty(); |

### DiscordMentionWebhookAdapterFooterTest
- 테스트 설명: DiscordMentionWebhookAdapter — footer 환경 라벨
- 위치: `src/test/java/com/umc/product/figma/adapter/out/external/DiscordMentionWebhookAdapterFooterTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [15](../../../src/test/java/com/umc/product/figma/adapter/out/external/DiscordMentionWebhookAdapterFooterTest.java#L15) | 환경=dev 면 footer 가 [ENV: dev] 로 시작한다 | 조건 환경=dev 면 footer 가 [ENV: dev] 로 시작한다 | 성공: 검증 assertThat(footer).startsWith("[ENV: dev] "); assertThat(footer).contains("Figma · "); assertThat(footer).endsWith("KST"); |
| [36](../../../src/test/java/com/umc/product/figma/adapter/out/external/DiscordMentionWebhookAdapterFooterTest.java#L36) | DiscordMentionWebhookAdapter — footer 환경 라벨 / 환경=prod 면 footer 가 [ENV: prod] 로 시작한다 | 조건 DiscordMentionWebhookAdapter — footer 환경 라벨 / 환경=prod 면 footer 가 [ENV: prod] 로 시작한다 | 성공: 검증 assertThat(footer).startsWith("[ENV: prod] "); |
| [53](../../../src/test/java/com/umc/product/figma/adapter/out/external/DiscordMentionWebhookAdapterFooterTest.java#L53) | DiscordMentionWebhookAdapter — footer 환경 라벨 / windowFrom / windowTo 가 모두 null 이어도 환경 라벨은 항상 prefix 된다 | 조건 DiscordMentionWebhookAdapter — footer 환경 라벨 / windowFrom / windowTo 가 모두 null 이어도 환경 라벨은 항상 prefix 된다 | 성공: 검증 assertThat(footer).isEqualTo("[ENV: staging] Figma comment forwarder"); |

### FigmaClassificationCacheAdapterTest
- 테스트 설명: FigmaClassificationCacheAdapter
- 위치: `src/test/java/com/umc/product/figma/adapter/out/cache/FigmaClassificationCacheAdapterTest.java`

| 라인 | 테스트 케이스 | 입력/조건 | 기대 결과 |
|---:|---|---|---|
| [16](../../../src/test/java/com/umc/product/figma/adapter/out/cache/FigmaClassificationCacheAdapterTest.java#L16) | positive classification cache를 저장하고 조회한다 | HTTP PUT comment-1; HTTP GET comment-1 | 성공: 검증 assertThat(adapter.contains("comment-1")).isTrue(); assertThat(adapter.get("comment-1")).contains("auth"); assertThat(cacheUseCase.lastSpec.namespace()).isEqualTo(CacheNamespace.FIGMA_CLASSIFICATION); assertThat(cache... |
| [33](../../../src/test/java/com/umc/product/figma/adapter/out/cache/FigmaClassificationCacheAdapterTest.java#L33) | FigmaClassificationCacheAdapter / negative classification cache는 contains=true와 Optional.empty로 조회된다 | HTTP PUT comment-1; HTTP GET comment-1 | 성공: 검증 assertThat(adapter.contains("comment-1")).isTrue(); assertThat(adapter.get("comment-1")).isEmpty(); |
| [44](../../../src/test/java/com/umc/product/figma/adapter/out/cache/FigmaClassificationCacheAdapterTest.java#L44) | FigmaClassificationCacheAdapter / 저장되지 않은 commentId는 contains=false이다 | HTTP GET missing | 성공: 검증 assertThat(adapter.contains("missing")).isFalse(); assertThat(adapter.get("missing")).isEmpty(); |
