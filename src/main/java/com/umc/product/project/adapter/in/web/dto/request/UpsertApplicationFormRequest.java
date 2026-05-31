package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.project.adapter.in.web.dto.common.ApplicationFormSection;
import com.umc.product.project.application.port.in.command.dto.UpsertApplicationFormCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 지원 폼 저장 요청 (PROJECT-106 PUT).
 * <p>
 * 본문이 곧 폼의 새 상태가 된다 (PUT 시멘틱). 빠진 섹션/질문/옵션은 삭제됨.
 * <p>
 * 필드 fallback:
 * <ul>
 *   <li>{@code title} 이 null 이면 Service 가 {@code Project.name} → {@code "프로젝트 지원서"} 순으로 폴백</li>
 *   <li>{@code sections} 가 빈 리스트면 모든 섹션 삭제</li>
 * </ul>
 */
public record UpsertApplicationFormRequest(

    @Size(max = 200, message = "폼 제목은 200자 이하여야 합니다")
    String title,

    @Size(max = 500, message = "폼 설명은 500자 이하여야 합니다")
    String description,

    @NotNull(message = "sections 필드는 필수입니다 (빈 리스트는 허용)")
    @Valid
    List<ApplicationFormSection> sections
) {

    public UpsertApplicationFormCommand toCommand(Long projectId, Long requesterMemberId) {
        return UpsertApplicationFormCommand.builder()
            .projectId(projectId)
            .requesterMemberId(requesterMemberId)
            .title(title)
            .description(description)
            .sections(sections.stream().map(ApplicationFormSection::toEntry).toList())
            .build();
    }
}
