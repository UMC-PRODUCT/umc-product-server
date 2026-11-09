package com.umc.product.project.application.port.in.command.dto;

import java.util.List;

import lombok.Builder;

/**
 * 챌린저 지원서 답변 수정 및 임시저장 Command (APPLY-002).
 * <p>
 * path의 applicationId로 본인의 DRAFT 또는 SUBMITTED 지원서를 명시적으로 식별한다.
 * PUT 시멘틱 — {@code answers}가 곧 새 전체 상태이며, 빠진 questionId의 기존 답변은 삭제된다.
 * DRAFT는 임시저장하고, SUBMITTED는 지원 기간과 제출 검증을 통과한 답변으로 갱신한다.
 */
@Builder
public record UpdateProjectApplicationDraftCommand(
    Long projectId,
    Long applicationId,
    Long requesterMemberId,
    List<AnswerEntry> answers
) {
    /**
     * 한 질문에 대한 응답 입력값. type 별 사용 필드는 Form {@code AnswerCommand}와 동일.
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
