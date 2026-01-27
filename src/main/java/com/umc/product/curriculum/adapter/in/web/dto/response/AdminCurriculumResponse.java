package com.umc.product.curriculum.adapter.in.web.dto.response;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.AdminCurriculumInfo;
import com.umc.product.curriculum.application.port.in.query.AdminCurriculumInfo.WorkbookInfo;
import com.umc.product.curriculum.domain.enums.MissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Admin 커리큘럼 조회 응답")
public record AdminCurriculumResponse(
        @Schema(description = "커리큘럼 ID", example = "1")
        Long id,

        @Schema(description = "파트", example = "SPRINGBOOT")
        ChallengerPart part,

        @Schema(description = "커리큘럼 제목", example = "9기 Springboot")
        String title,

        @Schema(description = "워크북 목록")
        List<WorkbookResponse> workbooks
) {
    public static AdminCurriculumResponse from(AdminCurriculumInfo info) {
        if(info == null) {
            return null;
        }

        List<WorkbookResponse> workbookResponses = info.workbooks().stream()
                .map(WorkbookResponse::from)
                .toList();

        return new AdminCurriculumResponse(
                info.id(),
                info.part(),
                info.title(),
                workbookResponses
        );
    }

    @Schema(description = "워크북 응답")
    public record WorkbookResponse(
            @Schema(description = "워크북 ID", example = "1")
            Long id,

            @Schema(description = "주차", example = "1")
            Integer weekNo,

            @Schema(description = "워크북 제목", example = "1주차 - Spring Boot 시작하기")
            String title,

            @Schema(description = "워크북 설명", example = "스프링 부트 기본 개념")
            String description,

            @Schema(description = "워크북 URL", example = "https://...")
            String workbookUrl,

            @Schema(description = "시작일", example = "2024-03-01")
            LocalDate startDate,

            @Schema(description = "종료일", example = "2024-03-07")
            LocalDate endDate,

            @Schema(description = "미션 타입", example = "LINK")
            MissionType missionType,

            @Schema(description = "배포일시", example = "2024-03-01T00:00:00")
            LocalDateTime releasedAt,

            @Schema(description = "배포 여부", example = "true")
            boolean isReleased
    ) {
        public static WorkbookResponse from(WorkbookInfo info) {
            return new WorkbookResponse(
                    info.id(),
                    info.weekNo(),
                    info.title(),
                    info.description(),
                    info.workbookUrl(),
                    info.startDate(),
                    info.endDate(),
                    info.missionType(),
                    info.releasedAt(),
                    info.isReleased()
            );
        }
    }
}
