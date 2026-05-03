package com.umc.product.project.application.port.in.command.dto;

import java.util.List;
import java.util.Objects;
import lombok.Builder;

/**
 * 챌린저 지원서 임시저장 Command (APPLY-002).
 * <p>
 * (projectId, requesterMemberId)로 본인의 PENDING 지원서를 식별 — applicationId 를 path에 노출하지 않는다.
 * PUT 시멘틱 — {@code answers}가 곧 새 전체 상태이며, 빠진 questionId의 기존 답변은 삭제된다.
 * Survey의 {@code ManageFormResponseUseCase.updateDraft}에 위임된다.
 */
@Builder
public record UpdateProjectApplicationDraftCommand(
    Long projectId,
    Long requesterMemberId,
    List<AnswerEntry> answers
) {
    /**
     * 한 질문에 대한 응답 입력값. type 별 사용 필드는 Survey {@code AnswerCommand}와 동일.
     */
    @Builder
    public record AnswerEntry(
        Long questionId,
        String textValue,
        List<Long> selectedOptionIds,
        List<String> fileIds
    ) {
    }
}
