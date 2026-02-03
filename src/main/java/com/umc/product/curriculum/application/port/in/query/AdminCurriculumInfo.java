package com.umc.product.curriculum.application.port.in.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.MissionType;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

public record AdminCurriculumInfo(
        Long id,
        ChallengerPart part,
        String title,
        List<WorkbookInfo> workbooks
) {
    public static AdminCurriculumInfo from(Curriculum curriculum) {
        List<WorkbookInfo> workbookInfos = curriculum.getOriginalWorkbooks().stream()
                .map(WorkbookInfo::from)
                .sorted(Comparator.comparing(WorkbookInfo::weekNo))
                .toList();

        return new AdminCurriculumInfo(
                curriculum.getId(),
                curriculum.getPart(),
                curriculum.getTitle(),
                workbookInfos
        );
    }

    public record WorkbookInfo(
            Long id,
            Integer weekNo,
            String title,
            String description,
            String workbookUrl,
            Instant startDate,
            Instant endDate,
            MissionType missionType,
            Instant releasedAt,
            boolean isReleased
    ) {
        public static WorkbookInfo from(OriginalWorkbook workbook) {
            return new WorkbookInfo(
                    workbook.getId(),
                    workbook.getWeekNo(),
                    workbook.getTitle(),
                    workbook.getDescription(),
                    workbook.getWorkbookUrl(),
                    workbook.getStartDate(),
                    workbook.getEndDate(),
                    workbook.getMissionType(),
                    workbook.getReleasedAt(),
                    workbook.isReleased()
            );
        }
    }
}
