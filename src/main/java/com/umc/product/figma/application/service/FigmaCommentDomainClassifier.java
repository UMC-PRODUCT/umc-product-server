package com.umc.product.figma.application.service;

import com.umc.product.figma.application.port.out.dto.FigmaCommentInfo;
import com.umc.product.llm.application.port.in.ChatCompleteUseCase;
import com.umc.product.llm.application.port.in.dto.ChatCompleteCommand;
import com.umc.product.llm.application.port.in.dto.ChatCompletionResult;
import com.umc.product.llm.domain.exception.LlmDomainException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Figma 댓글 본문을 LLM 으로 분석해 등록된 라우팅 도메인 키 중 하나로 분류한다. 후보 도메인 리스트는 운영진이 figma_routing_domain 에 등록한 도메인 키들에서 가져오며, LLM 응답이
 * 후보 외 값이거나 호출이 실패하면 null 을 반환해 호출자가 fallback 처리하도록 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FigmaCommentDomainClassifier {

    private static final String SYSTEM_PROMPT = """
        너는 Figma 디자인 파일에 달린 댓글을 읽고 그것이 서버 프로젝트의 어느 도메인과
        가장 관련 있는지 분류하는 라우터다. 반드시 후보 도메인 키 중 정확히 하나만,
        다른 설명 없이 그 키 문자열만 반환하라.
        """;

    private final ChatCompleteUseCase chatCompleteUseCase;

    /**
     * @return 매칭된 domain_key, 분류 실패 또는 후보 외 응답이면 null
     */
    public String classify(FigmaCommentInfo comment, List<String> candidateDomainKeys) {
        if (candidateDomainKeys == null || candidateDomainKeys.isEmpty()) {
            return null;
        }
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
}
