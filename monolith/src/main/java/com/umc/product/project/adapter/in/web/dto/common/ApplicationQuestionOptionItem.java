package com.umc.product.project.adapter.in.web.dto.common;

import com.umc.product.project.application.port.in.command.dto.UpsertApplicationFormCommand.ApplicationQuestionOptionEntry;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

/**
 * 지원 문항 선택지 (RADIO / CHECKBOX / DROPDOWN 타입에서만 의미 있음).
 * <p>
 * Request 와 Response 양쪽에서 재사용된다. {@code optionId} 가 null 이면 신규 추가, 있으면 기존 수정/이동.
 */
@Builder
public record ApplicationQuestionOptionItem(
    Long optionId,

    @NotBlank(message = "선택지 내용은 필수입니다")
    String content,

    @PositiveOrZero(message = "orderNo는 0 이상이어야 합니다")
    long orderNo,

    boolean isOther
) {

    public ApplicationQuestionOptionEntry toEntry() {
        return ApplicationQuestionOptionEntry.builder()
            .optionId(optionId)
            .content(content)
            .orderNo(orderNo)
            .isOther(isOther)
            .build();
    }

    public static ApplicationQuestionOptionItem from(ApplicationFormInfo.OptionInfo info) {
        return ApplicationQuestionOptionItem.builder()
            .optionId(info.optionId())
            .content(info.content())
            .orderNo(info.orderNo())
            .isOther(info.isOther())
            .build();
    }
}
