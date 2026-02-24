package com.umc.product.curriculum.adapter.in.web.dto.response;

import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "워크북 제출 현황")
public record WorkbookSubmissionResponse(
        @Schema(description = "챌린저 워크북 ID", example = "1")
        Long challengerWorkbookId,

        @Schema(description = "챌린저 ID", example = "10")
        Long challengerId,

        @Schema(description = "멤버 이름(실명)", example = "홍길동")
        String memberName,

        @Schema(description = "챌린저 닉네임", example = "길동이")
        String challengerName,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl,

        @Schema(description = "학교명", example = "서울대학교")
        String schoolName,

        @Schema(description = "파트", example = "SPRINGBOOT")
        String part,

        @Schema(description = "워크북 제목", example = "Week 1 - Spring 기초")
        String workbookTitle,

        @Schema(description = "워크북 상태", example = "SUBMITTED")
        String status
) {
    public static WorkbookSubmissionResponse from(WorkbookSubmissionInfo info) {
        return new WorkbookSubmissionResponse(
                info.challengerWorkbookId(),
                info.challengerId(),
                info.memberName(),
                info.challengerName(),
                info.profileImageUrl(),
                info.schoolName(),
                info.part(),
                info.workbookTitle(),
                info.status().name()
        );
    }
}
