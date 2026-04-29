package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.project.adapter.in.web.dto.common.ApplicationFormSection;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import java.util.List;
import lombok.Builder;

/**
 * 지원 폼 조회 응답 (PROJECT-106-GET).
 * <p>
 * 폼이 존재하지 않으면 Controller 가 {@code ApiResponse.result = null} 로 반환한다.
 */
// TODO: 챌린저 호출 시 본인 파트 + COMMON 섹션만 노출하는 forApplicant(part) 마스킹 메서드 추가
@Builder
public record GetApplicationFormResponse(
    Long projectId,
    Long applicationFormId,
    String title,
    String description,
    List<ApplicationFormSection> sections
) {

    public static GetApplicationFormResponse from(ApplicationFormInfo info) {
        return GetApplicationFormResponse.builder()
            .projectId(info.projectId())
            .applicationFormId(info.applicationFormId())
            .title(info.title())
            .description(info.description())
            .sections(info.sections().stream().map(ApplicationFormSection::from).toList())
            .build();
    }
}
