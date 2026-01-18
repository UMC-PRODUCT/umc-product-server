package com.umc.product.organization.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스터디 그룹이 있는 학교 목록 응답")
public record   StudyGroupSchoolsResponse(
        @Schema(description = "학교 목록")
        List<SchoolSummary> schools
) {

    @Schema(description = "학교 요약 정보")
    public record SchoolSummary(
            @Schema(description = "학교 ID", example = "1")
            Long schoolId,

            @Schema(description = "학교명", example = "서울대학교")
            String schoolName,

            @Schema(description = "학교 로고 이미지 URL")
            String logoImageUrl,

            @Schema(description = "총 스터디 그룹 수", example = "12")
            int totalStudyGroupCount,

            @Schema(description = "총 멤버 수", example = "48")
            int totalMemberCount
    ) {
    }
}
