package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

import com.umc.product.curriculum.application.port.in.query.dto.MyCurriculumInfo;

public enum WeeklyCurriculumStatus {
    NOT_STARTED, // 미션이 하나도 제출되지 않은 경우
    IN_PROGRESS, // 해당 주차에 속한 워크북들에 대해서 미션이 하나라도 제출된 경우
    AWAITING_RESULT, // 주차가 완료되고 결정을 기다리는 중
    BEST, PASS, FAIL, // 주차가 완료되고, 상태가 결정된 경우
    ;

    // TODO: enum도 도메인임, 내부에 도메인 로직으로 상태 연산해서 보내줄 수 있을 것
    // 의존성 발향성은 맞으니까 여기서 Info단은 알아도 무방할듯 (엔티티단까지는 조금 그럼)

    /**
     * TODO: MissionSubmission, MissionFeedback 데이터가 MyCurriculumInfo에 포함되면
     *       IN_PROGRESS, AWAITING_RESULT, BEST/PASS/FAIL 판별 로직 구현 필요.
     *       현재는 배포된 워크북 존재 여부만으로 단순 판별.
     */
    public static WeeklyCurriculumStatus from(MyCurriculumInfo.MyWeeklyCurriculumInfo myWeeklyCurriculumInfo) {
        boolean hasDeployedWorkbook = myWeeklyCurriculumInfo.releasedOriginalWorkbooks()
            .stream()
            .anyMatch(MyCurriculumInfo.MyOriginalWorkbookInfo::isDeployedToMember);
        return hasDeployedWorkbook ? IN_PROGRESS : NOT_STARTED;
    }
}
