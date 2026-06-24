package com.umc.product.figma.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.product.figma.application.port.out.FigmaClassificationCachePort;
import com.umc.product.figma.application.port.out.LoadFigmaCommentClassificationPort;
import com.umc.product.figma.application.port.out.SaveFigmaCommentClassificationPort;
import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import com.umc.product.llm.application.port.in.ChatCompleteUseCase;
import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.domain.exception.LlmDomainException;

import lombok.extern.slf4j.Slf4j;

/**
 * Figma 댓글 본문을 LLM 으로 분석해 등록된 라우팅 도메인 키 중 하나로 분류한다. 후보 도메인 리스트는 운영진이 figma_routing_domain 에 등록한 도메인 키들에서 가져오며, LLM 응답이
 * 후보 외 값이거나 호출이 실패하면 null 을 반환해 호출자가 fallback 처리하도록 한다.
 * <p>
 * 3-tier 캐시 전략으로 LLM 호출 횟수를 최소화한다 (ADR-006 §Decision 5):
 * <ol>
 *   <li>L1 - in-memory Caffeine 캐시 (5분 TTL): sync→preview 같은 짧은 시간 내 중복 호출 흡수.
 *       모든 분류(positive/negative/mock 결과) 를 보관.</li>
 *   <li>L2 - DB 영구 캐시 (figma_comment_classification): 재시작/다중 인스턴스 환경에서도
 *       동일 commentId 재호출 방지. mock 응답과 후보 외 응답은 보관하지 않음.</li>
 *   <li>L3 - LLM 호출 (마지막 수단).</li>
 * </ol>
 * batch 호출 ({@link #classifyBatch}) 은 한 파일의 댓글 N개를 단일 LLM 호출에 묶어
 * provider 의 RPM 한도 압박을 근본적으로 줄인다.
 */
@Slf4j
@Component
public class FigmaCommentDomainClassifier {

    private static final String SYSTEM_PROMPT = """
        너는 Figma 디자인 파일에 달린 댓글을 읽고 그것이 서버 프로젝트의 어느 도메인과
        가장 관련 있는지 분류하는 라우터다. 반드시 후보 도메인 키 중 정확히 하나만,
        다른 설명 없이 그 키 문자열만 반환하라.
        """;

    private static final String BATCH_SYSTEM_PROMPT = """
        너는 Figma 디자인 파일 댓글 여러 건을 받아 각 댓글을 후보 도메인 키 중 하나로 분류한다.
        반드시 다음 JSON 배열 형식으로만 응답하라. 다른 설명, 마크다운, 코드 블록은 절대 포함하지 마라.

        [{"commentId":"<원본 commentId>","domainKey":"<후보 키 중 하나>"}, ...]

        규칙:
        - 모든 입력 댓글에 대해 한 항목씩 응답한다.
        - domainKey 는 반드시 후보 도메인 키 목록 중 정확히 하나여야 한다.
        - 어떤 후보에도 명확히 들어맞지 않으면 가장 가까운 키를 선택한다 (null 금지).
        """;

    private static final String MOCK_PROVIDER = "mock";
    /**
     * batch 응답 한 항목의 보수적 토큰 추정값 (JSON 키/값 + 구분자 포함).
     */
    private static final int BATCH_TOKENS_PER_ITEM = 40;
    /**
     * batch 응답 JSON 외곽 (배열 괄호, 공백 등) 토큰 여유분.
     */
    private static final int BATCH_TOKENS_OVERHEAD = 32;
    /**
     * 단일 LLM 호출에 묶을 최대 댓글 수. provider 컨텍스트 한도 초과를 방지한다.
     */
    private static final int MAX_BATCH_SIZE = 30;

    private final ChatCompleteUseCase chatCompleteUseCase;
    private final ObjectMapper objectMapper;
    private final LoadFigmaCommentClassificationPort loadClassificationPort;
    private final SaveFigmaCommentClassificationPort saveClassificationPort;
    private final FigmaClassificationCachePort cachePort;

    public FigmaCommentDomainClassifier(
        ChatCompleteUseCase chatCompleteUseCase,
        ObjectMapper objectMapper,
        LoadFigmaCommentClassificationPort loadClassificationPort,
        SaveFigmaCommentClassificationPort saveClassificationPort,
        FigmaClassificationCachePort cachePort
    ) {
        this.chatCompleteUseCase = chatCompleteUseCase;
        this.objectMapper = objectMapper;
        this.loadClassificationPort = loadClassificationPort;
        this.saveClassificationPort = saveClassificationPort;
        this.cachePort = cachePort;
    }

    private static String stripMarkdownFence(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
            trimmed = trimmed.trim();
        }
        return trimmed;
    }

    /**
     * @return 매칭된 domain_key, 분류 실패 또는 후보 외 응답이면 null
     */
    public String classify(FigmaCommentInfo comment, List<String> candidateDomainKeys) {
        if (candidateDomainKeys == null || candidateDomainKeys.isEmpty()) {
            return null;
        }
        if (cachePort.contains(comment.commentId())) {
            Optional<String> cached = cachePort.get(comment.commentId());
            log.debug("LLM 분류 L1 캐시 히트: commentId={}, cached={}", comment.commentId(), cached.orElse(null));
            return cached.orElse(null);
        }
        Map<String, String> persisted = loadClassificationPort.findClassifications(List.of(comment.commentId()));
        String fromDb = persisted.get(comment.commentId());
        if (fromDb != null) {
            log.debug("LLM 분류 L2 DB 히트: commentId={}, domainKey={}", comment.commentId(), fromDb);
            cachePort.put(comment.commentId(), Optional.of(fromDb));
            return fromDb;
        }
        SingleClassifyOutcome outcome = doClassify(comment, candidateDomainKeys);
        // 호출 자체가 성공한 경우에만 결과를 캐싱한다 (positive 든 후보 외 응답이든).
        // 호출이 transient 예외로 실패한 경우는 캐싱하지 않아 다음 호출에서 즉시 재시도된다.
        if (outcome.callSucceeded()) {
            cachePort.put(comment.commentId(), Optional.ofNullable(outcome.picked()));
        }
        return outcome.picked();
    }

    /**
     * 댓글 N개를 한 번의 LLM 호출로 분류한다. L1 캐시 hit / L2 DB 히트 댓글은 LLM 호출에서 제외되며, 그래도 남은 미캐시 댓글만 batch prompt 에 포함된다.
     *
     * @return commentId → 매칭된 domain_key 의 Map (분류 실패/후보 외 응답인 댓글은 Map 에 없음)
     */
    public Map<String, String> classifyBatch(List<FigmaCommentInfo> comments, List<String> candidateDomainKeys) {
        if (comments == null || comments.isEmpty() || candidateDomainKeys == null || candidateDomainKeys.isEmpty()) {
            return Map.of();
        }

        Map<String, String> results = new LinkedHashMap<>();
        List<FigmaCommentInfo> afterMemoryCache = new ArrayList<>();
        for (FigmaCommentInfo c : comments) {
            if (cachePort.contains(c.commentId())) {
                cachePort.get(c.commentId()).ifPresent(domain -> results.put(c.commentId(), domain));
            } else {
                afterMemoryCache.add(c);
            }
        }

        if (!afterMemoryCache.isEmpty()) {
            List<String> ids = afterMemoryCache.stream().map(FigmaCommentInfo::commentId).toList();
            Map<String, String> fromDb = loadClassificationPort.findClassifications(ids);
            List<FigmaCommentInfo> uncached = new ArrayList<>();
            for (FigmaCommentInfo c : afterMemoryCache) {
                String dbDomain = fromDb.get(c.commentId());
                if (dbDomain != null) {
                    cachePort.put(c.commentId(), Optional.of(dbDomain));
                    results.put(c.commentId(), dbDomain);
                } else {
                    uncached.add(c);
                }
            }
            if (!uncached.isEmpty()) {
                for (List<FigmaCommentInfo> chunk : partition(uncached, MAX_BATCH_SIZE)) {
                    BulkClassifyOutcome bulk = doBulkClassify(chunk, candidateDomainKeys);
                    // bulk.provider() == null 은 doBulkClassify 의 catch 분기 (LLM 호출 자체 실패) 를 의미한다.
                    // 호출 실패 시 negative 캐시를 박지 않아 다음 사이클에서 즉시 재시도되도록 한다.
                    boolean callSucceeded = bulk.provider() != null;
                    for (FigmaCommentInfo c : chunk) {
                        String domain = bulk.results().get(c.commentId());
                        if (callSucceeded) {
                            cachePort.put(c.commentId(), Optional.ofNullable(domain));
                        }
                        if (domain != null) {
                            results.put(c.commentId(), domain);
                            persistIfEligible(c.commentId(), domain, bulk.provider());
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * 단건 LLM 분류. 호출 자체가 transient 예외로 실패하면 {@code callSucceeded=false} 를 반환해 호출자가 negative cache 박지 않도록 한다 (P1 fix).
     */
    private SingleClassifyOutcome doClassify(FigmaCommentInfo comment, List<String> candidateDomainKeys) {
        String userPrompt = buildUserPrompt(comment, candidateDomainKeys);
        try {
            // figma 도메인이 system/user prompt 모두 완성해 LLM 도메인에 보낸다.
            // 후보 제약은 SYSTEM_PROMPT 자체에 명시되어 있고 후보 목록은 user prompt 에 그대로 들어 있다.
            ChatCompletionResult result = chatCompleteUseCase.complete(
                ChatCompleteCommand.freeForm(SYSTEM_PROMPT, userPrompt)
            );
            String picked = result.text() == null ? null : result.text().trim();
            if (picked == null || !candidateDomainKeys.contains(picked)) {
                log.warn("LLM 분류 응답이 후보 외 값입니다. response={}, candidates={}", picked, candidateDomainKeys);
                // 호출은 성공했으나 응답이 후보에 매칭되지 않음 → callSucceeded=true 로 negative 캐싱 허용.
                return new SingleClassifyOutcome(null, true);
            }
            log.debug("LLM 분류를 완료했습니다: commentId={}, picked={}, provider={}",
                comment.commentId(), picked, result.provider());
            persistIfEligible(comment.commentId(), picked, result.provider());
            return new SingleClassifyOutcome(picked, true);
        } catch (LlmDomainException e) {
            log.warn("LLM 분류 호출 실패: commentId={}, error={}", comment.commentId(), e.getMessage());
            return new SingleClassifyOutcome(null, false);
        }
    }

    private BulkClassifyOutcome doBulkClassify(List<FigmaCommentInfo> comments, List<String> candidates) {
        String userPrompt = buildBatchUserPrompt(comments, candidates);
        int maxTokens = BATCH_TOKENS_PER_ITEM * comments.size() + BATCH_TOKENS_OVERHEAD;
        Set<String> inputIds = comments.stream()
            .map(FigmaCommentInfo::commentId)
            .collect(Collectors.toSet());
        try {
            ChatCompletionResult result = chatCompleteUseCase.complete(
                ChatCompleteCommand.freeFormWithMaxTokens(BATCH_SYSTEM_PROMPT, userPrompt, maxTokens)
            );
            Map<String, String> parsed = parseBatchResponse(result.text(), candidates, inputIds);
            return new BulkClassifyOutcome(parsed, result.provider());
        } catch (LlmDomainException e) {
            log.warn("LLM batch 분류 호출 실패: count={}, error={}", comments.size(), e.getMessage());
            return new BulkClassifyOutcome(Map.of(), null);
        }
    }

    private void persistIfEligible(String commentId, String domainKey, String provider) {
        if (provider == null || MOCK_PROVIDER.equalsIgnoreCase(provider)) {
            return;
        }
        try {
            saveClassificationPort.save(commentId, domainKey, provider);
        } catch (RuntimeException e) {
            log.warn("LLM 분류 결과 영구 캐시 저장 실패 (분류 자체는 성공). commentId={}, error={}", commentId, e.toString());
        }
    }

    private String buildUserPrompt(FigmaCommentInfo comment, List<String> candidates) {
        return String.format("""
                [후보 도메인 키]
                %s

                [댓글 작성자]
                %s

                [댓글 본문]
                %s
                """,
            String.join(", ", candidates),
            comment.authorName(),
            comment.message() == null ? "" : comment.message()
        );
    }

    private String buildBatchUserPrompt(List<FigmaCommentInfo> comments, List<String> candidates) {
        StringBuilder sb = new StringBuilder();
        sb.append("[후보 도메인 키]\n").append(String.join(", ", candidates)).append("\n\n");
        sb.append("[댓글 목록]\n");
        for (FigmaCommentInfo c : comments) {
            sb.append("- commentId: ").append(c.commentId()).append('\n');
            sb.append("  작성자: ").append(c.authorName() == null ? "" : c.authorName()).append('\n');
            String message = c.message() == null ? "" : c.message().replace("\n", " ");
            sb.append("  본문: ").append(message).append('\n');
        }
        return sb.toString();
    }

    private Map<String, String> parseBatchResponse(
        String raw,
        List<String> candidates,
        Set<String> inputCommentIds
    ) {
        if (raw == null || raw.isBlank()) {
            log.warn("LLM batch 분류 응답이 비어 있습니다.");
            return Map.of();
        }
        String json = stripMarkdownFence(raw.trim());
        try {
            List<Map<String, String>> parsed = objectMapper.readValue(json, new TypeReference<>() {
            });
            Map<String, String> map = new HashMap<>();
            for (Map<String, String> item : parsed) {
                String commentId = item.get("commentId");
                String domainKey = item.get("domainKey");
                if (commentId == null || domainKey == null) {
                    continue;
                }
                if (!inputCommentIds.contains(commentId)) {
                    log.warn("LLM 응답에 입력에 없는 commentId 포함 (할루시네이션 의심): {}", commentId);
                    continue;
                }
                if (!candidates.contains(domainKey)) {
                    log.warn("LLM batch 분류 응답이 후보 외 값입니다. commentId={}, domainKey={}", commentId, domainKey);
                    continue;
                }
                map.put(commentId, domainKey);
            }
            return map;
        } catch (Exception e) {
            log.warn("LLM batch 분류 응답 JSON 파싱 실패: {}", e.toString());
            return Map.of();
        }
    }

    private record BulkClassifyOutcome(Map<String, String> results, String provider) {
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    /**
     * 단건 분류 결과. {@code callSucceeded=false} 는 LLM 호출 자체가 transient 예외로 실패한 케이스를 표현해 호출자가 negative cache 박지 않도록 한다.
     */
    private record SingleClassifyOutcome(String picked, boolean callSucceeded) {
    }
}
