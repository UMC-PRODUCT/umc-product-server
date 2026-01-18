package com.umc.product.organization.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 스터디 그룹 목록 관련 DTO
 * CursorResponse&lt;StudyGroupSummary&gt; 형태로 사용됩니다.
 */
public final class StudyGroupListResponse {

    private StudyGroupListResponse() {
    }

    @Schema(description = "스터디 그룹 요약 정보")
    public record StudyGroupSummary(
            @Schema(description = "스터디 그룹 ID", example = "1")
            Long groupId,

            @Schema(description = "스터디 그룹명", example = "React A팀")
            String name,

            @Schema(description = "멤버 수", example = "4")
            int memberCount,

            @Schema(description = "리더 정보")
            LeaderSummary leader
    ) {
    }

    @Schema(description = "리더 요약 정보")
    public record LeaderSummary(
            @Schema(description = "챌린저 ID", example = "101")
            Long challengerId,

            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "프로필 이미지 URL")
            String profileImageUrl
    ) {
    }
}
