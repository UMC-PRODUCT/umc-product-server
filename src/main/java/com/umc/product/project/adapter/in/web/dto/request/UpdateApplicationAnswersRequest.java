package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.project.adapter.in.web.dto.common.ApplicationAnswerItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

/**
 * 챌린저 지원서 임시저장 요청 (APPLY-002).
 * <p>
 * PUT 시멘틱 — {@code answers} 가 곧 새 전체 상태. 빠진 questionId 의 기존 답변은 삭제됨.
 * 빈 리스트 허용 (모든 답변 제거).
 */
@Builder
public record UpdateApplicationAnswersRequest(
    @NotNull(message = "answers는 null 일 수 없습니다 (빈 리스트는 허용)")
    @Valid
    List<ApplicationAnswerItem> answers
) {
}
