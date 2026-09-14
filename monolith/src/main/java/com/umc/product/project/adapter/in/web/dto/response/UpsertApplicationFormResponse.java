package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.project.adapter.in.web.dto.common.ApplicationFormSection;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import java.util.List;
import lombok.Builder;

/**
 * 지원 폼 저장 응답 (PROJECT-106 PUT).
 * <p>
 * 저장 후 폼의 전체 구조를 반환한다. PM 편집 화면이 즉시 갱신된 결과를 그릴 수 있도록 함.
 */
@Builder
public record UpsertApplicationFormResponse(
    Long projectId,
    Long applicationFormId,
    String title,
    String description,
    List<ApplicationFormSection> sections
) {

    public static UpsertApplicationFormResponse from(ApplicationFormInfo info) {
        return UpsertApplicationFormResponse.builder()
            .projectId(info.projectId())
            .applicationFormId(info.applicationFormId())
            .title(info.title())
            .description(info.description())
            .sections(info.sections().stream().map(ApplicationFormSection::from).toList())
            .build();
    }
}
