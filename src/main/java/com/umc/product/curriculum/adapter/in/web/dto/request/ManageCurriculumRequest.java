package com.umc.product.curriculum.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.command.CurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.CurriculumCommand.WorkbookCommand;
import com.umc.product.curriculum.domain.enums.MissionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Schema(description = "커리큘럼 관리 요청")
public record ManageCurriculumRequest(
        @Schema(description = "파트", example = "SPRINGBOOT")
        @NotNull(message = "파트는 필수입니다")
        ChallengerPart part,

        @Schema(description = "커리큘럼 제목", example = "9기 Springboot")
        @NotBlank(message = "커리큘럼 제목은 필수입니다")
        String title,

        @Schema(description = "워크북 목록")
        @NotNull(message = "워크북 목록은 필수입니다")
        @Valid
        List<WorkbookRequest> workbooks
) {
    public CurriculumCommand toCommand() {
        List<WorkbookCommand> workbookCommands = workbooks.stream()
                .map(WorkbookRequest::toCommand)
                .toList();

        return new CurriculumCommand(part, title, workbookCommands);
    }

    @Schema(description = "워크북 요청")
    public record WorkbookRequest(
            @Schema(description = "워크북 ID (수정 시 필수, 생성 시 null)", example = "1")
            Long id,

            @Schema(description = "주차", example = "1")
            @NotNull(message = "주차는 필수입니다")
            Integer weekNo,

            @Schema(description = "워크북 제목", example = "1주차 - Spring Boot 시작하기")
            @NotBlank(message = "워크북 제목은 필수입니다")
            String title,

            @Schema(description = "워크북 설명", example = "스프링 부트 기본 개념")
            String description,

            @Schema(description = "워크북 URL", example = "https://...")
            String workbookUrl,

            @Schema(description = "시작일 (미입력 시 기본값: 2099-12-31)", example = "2024-03-01T00:00:00Z")
            Instant startDate,

            @Schema(description = "종료일 (미입력 시 기본값: 2099-12-31)", example = "2024-03-07T23:59:59Z")
            Instant endDate,

            @Schema(description = "미션 타입 (미입력 시 기본값: LINK)", example = "LINK")
            MissionType missionType
    ) {
        public WorkbookCommand toCommand() {
            return new WorkbookCommand(id, weekNo, title, description, workbookUrl, startDate, endDate, missionType);
        }
    }
}
