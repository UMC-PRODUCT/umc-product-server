package com.umc.product.project.adapter.in.web.dto.common;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

/**
 * 챌린저 지원서 한 질문의 답변 입력 항목 (APPLY-002 Request).
 * <p>
 * 질문 type 별 사용 필드:
 * <ul>
 *   <li>SHORT_TEXT / LONG_TEXT -> {@code textValue}</li>
 *   <li>RADIO / DROPDOWN -> {@code selectedOptionIds} 1개</li>
 *   <li>CHECKBOX -> {@code selectedOptionIds} 1개 이상</li>
 *   <li>FILE -> {@code fileIds} 1개 이상</li>
 *   <li>PORTFOLIO -> {@code textValue} (링크) 또는 {@code fileIds}</li>
 * </ul>
 */
@Builder
public record ApplicationAnswerItem(
    @NotNull(message = "questionId는 필수입니다")
    Long questionId,
    String textValue,
    List<Long> selectedOptionIds,
    List<String> fileIds
) {
}
