package com.umc.product.figma.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import com.umc.product.llm.application.port.in.ChatCompleteUseCase;
import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.domain.exception.LlmDomainException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Figma 댓글 본문을 LLM 으로 분석해 등록된 라우팅 도메인 키 중 하나로 분류한다.
 * 후보 도메인 리스트는 운영진이 figma_routing_domain 에 등록한 도메인 키들에서 가져오며,
 * LLM 응답이 후보 외 값이거나 호출이 실패하면 null 을 반환해 호출자가 fallback 처리하도록 한다.
 * <p>
 * sync → preview 같은 짧은 시간 내 동일 댓글 중복 호출을 흡수하기 위해 commentId 키로
 * 단기 캐시 (5분 TTL, max 10k) 를 둔다 (ADR-006 §Decision 5).
 * <p>
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

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final long CACHE_MAX_SIZE = 10_000L;
    /** batch 응답 한 항목의 보수적 토큰 추정값 (JSON 키/값 + 구분자 포함). */
    private static final int BATCH_TOKENS_PER_ITEM = 40;
    /** batch 응답 JSON 외곽 (배열 괄호, 공백 등) 토큰 여유분. */
    private static final int BATCH_TOKENS_OVERHEAD = 32;

    private final ChatCompleteUseCase chatCompleteUseCase;
    private final ObjectMapper objectMapper;
    private final Cache<String, Optional<String>> cache;

    public FigmaCommentDomainClassifier(ChatCompleteUseCase chatCompleteUseCase, ObjectMapper objectMapper) {
        this.chatCompleteUseCase = chatCompleteUseCase;
        this.objectMapper = objectMapper;
        this.cache = Caffeine.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .expireAfterWrite(CACHE_TTL)
            .build();
    }

    /**
     * @return 매칭된 domain_key, 분류 실패 또는 후보 외 응답이면 null
     */
    public String classify(FigmaCommentInfo comment, List<String> candidateDomainKeys) {
        if (candidateDomainKeys == null || candidateDomainKeys.isEmpty()) {
            return null;
        }
        Optional<String> cached = cache.getIfPresent(comment.commentId());
        if (cached != null) {
            log.debug("LLM 분류 캐시 히트: commentId={}, cached={}", comment.commentId(), cached.orElse(null));
            return cached.orElse(null);
        }
        String picked = doClassify(comment, candidateDomainKeys);
        cache.put(comment.commentId(), Optional.ofNullable(picked));
        return picked;
    }

    /**
     * 댓글 N개를 한 번의 LLM 호출로 분류한다. 캐시 hit 댓글은 LLM 호출에서 제외되며
     * 캐시 miss 댓글만 batch prompt 에 포함된다.
     *
     * @return commentId → 매칭된 domain_key 의 Map (분류 실패/후보 외 응답인 댓글은 Map 에 없음)
     */
    public Map<String, String> classifyBatch(List<FigmaCommentInfo> comments, List<String> candidateDomainKeys) {
        if (comments == null || comments.isEmpty() || candidateDomainKeys == null || candidateDomainKeys.isEmpty()) {
            return Map.of();
        }

        Map<String, String> results = new LinkedHashMap<>();
        List<FigmaCommentInfo> uncached = new ArrayList<>();
        for (FigmaCommentInfo c : comments) {
            Optional<String> cached = cache.getIfPresent(c.commentId());
            if (cached != null) {
                cached.ifPresent(domain -> results.put(c.commentId(), domain));
            } else {
                uncached.add(c);
            }
        }

        if (uncached.isEmpty()) {
            return results;
        }

        Map<String, String> bulkResults = doBulkClassify(uncached, candidateDomainKeys);
        for (FigmaCommentInfo c : uncached) {
            String domain = bulkResults.get(c.commentId());
            cache.put(c.commentId(), Optional.ofNullable(domain));
            if (domain != null) {
                results.put(c.commentId(), domain);
            }
        }
        return results;
    }

    private String doClassify(FigmaCommentInfo comment, List<String> candidateDomainKeys) {
        String userPrompt = buildUserPrompt(comment, candidateDomainKeys);
        try {
            ChatCompletionResult result = chatCompleteUseCase.complete(
                ChatCompleteCommand.classify(SYSTEM_PROMPT, userPrompt, candidateDomainKeys)
            );
            String picked = result.text() == null ? null : result.text().trim();
            if (picked == null || !candidateDomainKeys.contains(picked)) {
                log.warn("LLM 분류 응답이 후보 외 값입니다. response={}, candidates={}", picked, candidateDomainKeys);
                return null;
            }
            log.debug("LLM 분류 성공: commentId={}, picked={}, provider={}",
                comment.commentId(), picked, result.provider());
            return picked;
        } catch (LlmDomainException e) {
            log.warn("LLM 분류 호출 실패: commentId={}, error={}", comment.commentId(), e.getMessage());
            return null;
        }
    }

    private Map<String, String> doBulkClassify(List<FigmaCommentInfo> comments, List<String> candidates) {
        String userPrompt = buildBatchUserPrompt(comments, candidates);
        int maxTokens = BATCH_TOKENS_PER_ITEM * comments.size() + BATCH_TOKENS_OVERHEAD;
        try {
            ChatCompletionResult result = chatCompleteUseCase.complete(
                ChatCompleteCommand.freeFormWithMaxTokens(BATCH_SYSTEM_PROMPT, userPrompt, maxTokens)
            );
            return parseBatchResponse(result.text(), candidates);
        } catch (LlmDomainException e) {
            log.warn("LLM batch 분류 호출 실패: count={}, error={}", comments.size(), e.getMessage());
            return Map.of();
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

    private Map<String, String> parseBatchResponse(String raw, List<String> candidates) {
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
}
