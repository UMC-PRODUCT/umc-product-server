package com.umc.product.curriculum.adapter.in.web.dto.response;

import com.umc.product.curriculum.application.port.in.query.CurriculumProgressInfo;
import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "커리큘럼 진행 상황")
public record CurriculumProgressResponse(
        @Schema(description = "커리큘럼 ID", example = "1")
        Long curriculumId,

        @Schema(description = "커리큘럼 제목", example = "9기 Springboot")
        String curriculumTitle,

        @Schema(description = "파트", example = "SPRINGBOOT")
        String part,

        @Schema(description = "완료 개수", example = "2")
        int completedCount,

        @Schema(description = "전체 개수", example = "8")
        int totalCount,

        @Schema(description = "주차별 워크북 목록")
        List<WorkbookProgress> workbooks
) {

    public static CurriculumProgressResponse from(CurriculumProgressInfo info) {
        return new CurriculumProgressResponse(
                info.curriculumId(),
                info.curriculumTitle(),
                info.part(),
                info.completedCount(),
                info.totalCount(),
                info.workbooks().stream()
                        .map(WorkbookProgress::from)
                        .toList()
        );
    }

    @Schema(description = "워크북 진행 정보")
    public record WorkbookProgress(
            @Schema(description = "챌린저 워크북 ID (미배포/미생성 시 null)")
            Long challengerWorkbookId,

            @Schema(description = "주차", example = "1")
            Integer weekNo,

            @Schema(description = "워크북 제목", example = "1주차 - Spring Boot 시작하기")
            String title,

            @Schema(description = "워크북 설명", example = "스프링 부트 기본 개념을 학습합니다")
            String description,

            @Schema(description = "미션 타입", example = "LINK")
            MissionType missionType,

            @Schema(description = "워크북 상태", example = "PENDING")
            WorkbookStatus status,

            @Schema(description = "워크북 배포 여부", example = "true")
            boolean isReleased,

            @Schema(description = "현재 진행 중인 주차 여부", example = "true")
            boolean isInProgress
    ) {

        public static WorkbookProgress from(CurriculumProgressInfo.WorkbookProgressInfo info) {
            return new WorkbookProgress(
                    info.challengerWorkbookId(),
                    info.weekNo(),
                    info.title(),
                    info.description(),
                    info.missionType(),
                    info.status(),
                    info.isReleased(),
                    info.isInProgress()
            );
        }
    }
}
