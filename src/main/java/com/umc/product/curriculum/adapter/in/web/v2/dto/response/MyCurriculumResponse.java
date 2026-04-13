package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookType;
import java.time.Instant;
import java.util.List;

/**
 * 커리큘럼, 주차별 커리큘럼, 원본 워크북, 챌린저 워크북, 미션 제출물, 피드백 정보, 베스트 워크북 정보를 나타내는 DTO
 */
public record MyCurriculumResponse(
    Long curriculumId,
    String title,
    List<MyWeeklyCurriculumResponse> weeks
) {

    /**
     * 사용자에 따른 각 주차별 커리큘럼과 관련된 내용
     * <p>
     * 각 주차별 기간은 미션 제출이 가능한 기긴과는 무관합니다.
     *
     * @param weeklyCurriculumId      PK
     * @param weekNo                  몇 주차인지
     * @param title                   주차별 커리큘럼 대제목
     * @param isExtra                 부록 여부
     * @param startsAt                주차 시작 기간
     * @param endsAt                  주차 종료 기간
     * @param isRequesterBestWorkbook 주차별 워크북이 베스트 워크북으로 선정되었는지 여부
     * @param originalWorkbooks       해당 주차에 대해서 배포 완료된 원본 워크북 목록
     */
    public record MyWeeklyCurriculumResponse(
        Long weeklyCurriculumId,
        Long weekNo,
        String title,
        boolean isExtra,
        Instant startsAt,
        Instant endsAt,
        boolean isRequesterBestWorkbook,
        List<MyOriginalWorkbookResponse> originalWorkbooks
    ) {
    }

    /**
     * 요청자에 따른 원본 워크북과 관련된 정보를 제공하는 DTO
     * <p>
     * OriginalWorkbook의 content는 API가 너무 무거워져 분리합니다.
     *
     * @param originalWorkbookId PK
     * @param title              원본 워크북 제목
     * @param description        (nullable) 원본 워크북 설명
     * @param url                (nullable) 원본 워크북 URL
     * @param type               워크북 유형 (메인/부록 여부)
     * @param missions           워크북 미션과 관련된 정보
     * @param isDeployedToMember 원본 워크북이 요청자에게 배포된 상태인지 여부 (ChallengerWorkbook이 존재하는지)
     * @param challengerWorkbook 원본 워크북을 배포받은 경우 그 챌린저 워크북과 관련된 정보
     */
    public record MyOriginalWorkbookResponse(
        Long originalWorkbookId,
        String title,
        String description,
        String url,
        OriginalWorkbookType type,
        List<MyOriginalWorkbookMissionResponse> missions,
        boolean isDeployedToMember,
        ChallengerWorkbookResponse challengerWorkbook
    ) {
    }

    /**
     * 요청자에 따른 원본 워크북 미션과 관련된 정보를 제공하는 DTO
     *
     * @param originalWorkbookMissionId PK
     * @param title                     미션 제목
     * @param description               미션 설명 (nullable)
     * @param missionType               미션 유형
     * @param isNecessary               미션 필수 수행 여부
     * @param hasSubmissions            미션 제출물이 존재하는지 여부
     * @param missionSubmission         요청자가 제출한 미션에 대한 정보, 미션 당 제출물은 한 개로 제한됨
     */
    public record MyOriginalWorkbookMissionResponse(
        Long originalWorkbookMissionId,
        String title,
        String description,
        MissionType missionType,
        boolean isNecessary,
        boolean hasSubmissions,
        MissionSubmissionResponse missionSubmission
    ) {
    }
}
