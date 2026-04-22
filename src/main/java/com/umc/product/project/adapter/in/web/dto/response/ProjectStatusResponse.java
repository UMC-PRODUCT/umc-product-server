package com.umc.product.project.adapter.in.web.dto.response;

import com.umc.product.project.application.port.in.query.dto.ProjectInfo;
import com.umc.product.project.domain.enums.ProjectStatus;
import lombok.Builder;

/**
 * 상태 변경을 수반하는 API들의 통일 응답 (PROJECT-101, 102, 107, 105, 108).
 * <p>
 * "이 호출 이후의 현재 상태"를 알려주는 최소 응답입니다. 마스킹 대상 필드 없음.
 */
@Builder
public record ProjectStatusResponse(
    Long projectId,
    ProjectStatus status
) {
    public static ProjectStatusResponse of(Long projectId, ProjectStatus status) {
        return new ProjectStatusResponse(projectId, status);
    }

    public static ProjectStatusResponse from(ProjectInfo info) {
        return new ProjectStatusResponse(info.id(), info.status());
    }
}
