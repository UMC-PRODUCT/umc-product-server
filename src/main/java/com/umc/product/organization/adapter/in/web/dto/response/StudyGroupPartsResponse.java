package com.umc.product.organization.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "특정 학교의 파트별 스터디 그룹 요약 응답")
public record StudyGroupPartsResponse(
        @Schema(description = "학교 ID", example = "1")
        Long schoolId,

        @Schema(description = "학교명", example = "서울대학교")
        String schoolName,

        @Schema(description = "파트 목록")
        List<PartSummary> parts
) {

    @Schema(description = "파트 요약 정보")
    public record PartSummary(
            @Schema(description = "파트", example = "WEB")
            String part,

            @Schema(description = "파트 표시명", example = "웹")
            String partDisplayName,

            @Schema(description = "스터디 그룹 수", example = "3")
            int studyGroupCount,

            @Schema(description = "멤버 수", example = "12")
            int memberCount
    ) {
    }
}
