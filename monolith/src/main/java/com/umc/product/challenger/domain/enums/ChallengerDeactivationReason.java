package com.umc.product.challenger.domain.enums;

import com.umc.product.common.domain.enums.ChallengerStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengerDeactivationReason {

    // 자의 탈부
    VOLUNTARY_ACADEMIC_AND_SCHEDULE(ChallengerStatus.WITHDRAWN), // 학업 및 일정 병행의 어려움
    VOLUNTARY_CAREER_PREPARATION(ChallengerStatus.WITHDRAWN), // 취업 및 진로 준비
    VOLUNTARY_PERSONAL_CIRCUMSTANCES(ChallengerStatus.WITHDRAWN), // 개인 사정
    VOLUNTARY_LEARNING_DIFFICULTY_AND_WORKLOAD(ChallengerStatus.WITHDRAWN), // 학습 난이도 및 학습량 부담
    VOLUNTARY_ACTIVITY_EXPECTATION_MISMATCH(ChallengerStatus.WITHDRAWN), //활동 내용과 기대의 차이
    VOLUNTARY_COMMUNICATION_AND_RELATIONSHIP(ChallengerStatus.WITHDRAWN), // 소통 및 관계의 어려움
    VOLUNTARY_OTHER(ChallengerStatus.WITHDRAWN), // 기타

    // 타의 탈부
    INVOLUNTARY_POINT_THRESHOLD(ChallengerStatus.EXPELLED), // 상벌점 합계 -20점
    INVOLUNTARY_EVENT_NO_SHOW(ChallengerStatus.EXPELLED), // 행사 노쇼
    INVOLUNTARY_LOW_PROJECT_PARTICIPATION(ChallengerStatus.EXPELLED), // 프로젝트 참여 저조
    INVOLUNTARY_OTHER(ChallengerStatus.EXPELLED); // 기타

    private final ChallengerStatus status;

    public boolean isOther() {
        return this == VOLUNTARY_OTHER || this == INVOLUNTARY_OTHER;
    }
}
