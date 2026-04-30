package com.umc.product.project.adapter.in.web.dto.request;

import com.umc.product.project.application.port.in.command.dto.UpdateProjectCommand;
import jakarta.validation.constraints.Size;

/**
 * 프로젝트 기본 정보 수정 요청 (PROJECT-102). 모든 필드 nullable 부분 갱신.
 */
public record UpdateProjectRequest(
    @Size(max = 100, message = "프로젝트명은 100자 이하여야 합니다")
    String name,

    @Size(max = 200, message = "설명은 200자 이하여야 합니다")
    String description,

    @Size(max = 300, message = "외부 링크는 300자 이하여야 합니다")
    String externalLink,

    String thumbnailFileId,

    String logoFileId
) {
    public UpdateProjectCommand toCommand(Long projectId, Long requesterMemberId) {
        return UpdateProjectCommand.builder()
            .projectId(projectId)
            .requesterMemberId(requesterMemberId)
            .name(name)
            .description(description)
            .externalLink(externalLink)
            .thumbnailFileId(thumbnailFileId)
            .logoFileId(logoFileId)
            .build();
    }
}
