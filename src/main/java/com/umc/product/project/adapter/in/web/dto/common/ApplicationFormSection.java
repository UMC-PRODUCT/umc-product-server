package com.umc.product.project.adapter.in.web.dto.common;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.dto.UpsertApplicationFormCommand.ApplicationFormSectionEntry;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import com.umc.product.project.domain.enums.FormSectionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Set;
import lombok.Builder;

/**
 * 지원 폼 섹션. {@code sectionId} 가 null 이면 신규 추가, 있으면 기존 수정/이동.
 * <p>
 * {@link FormSectionType#COMMON} 은 모든 파트에게 노출되며 {@code allowedParts} 가 무시된다.
 * {@link FormSectionType#PART} 는 {@code allowedParts} 가 1개 이상이어야 한다 (Service 레벨에서 검증).
 */
@Builder
public record ApplicationFormSection(
    Long sectionId,

    @NotNull(message = "섹션 타입은 필수입니다")
    FormSectionType type,

    Set<ChallengerPart> allowedParts,

    @NotBlank(message = "섹션 제목은 필수입니다")
    String title,

    String description,

    @PositiveOrZero(message = "orderNo는 0 이상이어야 합니다")
    int orderNo,

    @NotNull(message = "질문 리스트는 null 일 수 없습니다 (빈 리스트는 허용)")
    @Valid
    List<ApplicationQuestionItem> questions
) {

    public ApplicationFormSectionEntry toEntry() {
        return ApplicationFormSectionEntry.builder()
            .sectionId(sectionId)
            .type(type)
            .allowedParts(allowedParts == null ? Set.of() : allowedParts)
            .title(title)
            .description(description)
            .orderNo(orderNo)
            .questions(questions.stream().map(ApplicationQuestionItem::toEntry).toList())
            .build();
    }

    public static ApplicationFormSection from(ApplicationFormInfo.SectionInfo info) {
        return ApplicationFormSection.builder()
            .sectionId(info.sectionId())
            .type(info.type())
            .allowedParts(info.allowedParts())
            .title(info.title())
            .description(info.description())
            .orderNo((int) info.orderNo())
            .questions(info.questions().stream().map(ApplicationQuestionItem::from).toList())
            .build();
    }
}
