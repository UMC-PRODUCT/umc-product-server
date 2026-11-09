package com.umc.product.project.adapter.in.web.dto.request;

import java.util.List;

import com.umc.product.project.adapter.in.web.dto.common.ApplicationAnswerItem;
import com.umc.product.project.application.port.in.command.dto.UpdateProjectApplicationDraftCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 챌린저 지원서 답변 수정 및 임시저장 요청 (APPLY-002).
 * <p>
 * PUT 시멘틱 — {@code answers} 가 곧 새 전체 상태. 빠진 questionId 의 기존 답변은 삭제됨.
 * 빈 리스트는 요청 형식상 허용하지만, SUBMITTED 수정 시 필수 답변이 누락되면 저장을 거절한다.
 */
@Builder
public record UpdateApplicationAnswersRequest(
    @NotNull(message = "answers는 null 일 수 없습니다 (빈 리스트는 허용)")
    @Valid List<ApplicationAnswerItem> answers
) {

    public UpdateProjectApplicationDraftCommand toCommand(
        Long projectId,
        Long applicationId,
        Long requesterMemberId
    ) {
        return UpdateProjectApplicationDraftCommand.builder()
            .projectId(projectId)
            .applicationId(applicationId)
            .requesterMemberId(requesterMemberId)
            .answers(answers.stream().map(ApplicationAnswerItem::toEntry).toList())
            .build();
    }
}
