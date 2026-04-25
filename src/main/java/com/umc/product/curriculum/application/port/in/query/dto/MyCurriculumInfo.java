package com.umc.product.curriculum.application.port.in.query.dto;

import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * V2 내 커리큘럼 진행 상황 조회 결과 - WeeklyCurriculum → OriginalWorkbook → Mission 중첩 구조
 */
public record MyCurriculumInfo(
    Long curriculumId,
    String title,
    List<MyWeeklyCurriculumInfo> weeks
) {

    public record MyWeeklyCurriculumInfo(
        Long weeklyCurriculumId,
        Long weekNo,
        String title,
        boolean isExtra,
        Instant startsAt,
        Instant endsAt,
        List<MyOriginalWorkbookInfo> releasedOriginalWorkbooks
    ) {
    }

    public record MyOriginalWorkbookInfo(
        Long originalWorkbookId,
        String title,
        String description,
        String url,
        OriginalWorkbookType type,
        List<MyOriginalWorkbookMissionInfo> missions,
        // challengerWorkbookId == null 이면 이 멤버에게 배포되지 않은 워크북
        Optional<Long> challengerWorkbookId
    ) {
        public boolean isDeployedToMember() {
            return challengerWorkbookId.isPresent();
        }
    }

    public record MyOriginalWorkbookMissionInfo(
        Long originalWorkbookMissionId,
        String title,
        String description,
        MissionType missionType,
        boolean isNecessary
        // TODO: MissionSubmission 정보 (hasSubmission, submission) 추가 필요
        //  - LoadMissionSubmissionPort 구현 후 서비스에서 조회 로직 추가
    ) {
    }
}
