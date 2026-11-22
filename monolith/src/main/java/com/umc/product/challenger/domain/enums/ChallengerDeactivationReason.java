package com.umc.product.challenger.domain.enums;

import com.umc.product.common.domain.enums.ChallengerStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengerDeactivationReason {

    // 자의 탈부
    VOLUNTARY_ACADEMIC_AND_SCHEDULE(
        ChallengerStatus.WITHDRAWN,
        "학업 및 일정 병행의 어려움",
        "수업, 시험, 다른 대외활동과 병행"
    ),
    VOLUNTARY_CAREER_PREPARATION(
        ChallengerStatus.WITHDRAWN,
        "취업 및 진로 준비",
        "취업 준비, 인턴, 진로 변경"
    ),
    VOLUNTARY_PERSONAL_CIRCUMSTANCES(
        ChallengerStatus.WITHDRAWN,
        "개인 사정",
        "건강, 가정 사정 등"
    ),
    VOLUNTARY_LEARNING_DIFFICULTY_AND_WORKLOAD(
        ChallengerStatus.WITHDRAWN,
        "학습 난이도 및 학습량 부담",
        "워크북 난이도, 과제량, 학습 진도"
    ),
    VOLUNTARY_ACTIVITY_EXPECTATION_MISMATCH(
        ChallengerStatus.WITHDRAWN,
        "활동 내용과 기대의 차이",
        "희망한 학습·프로젝트 경험과 실제 활동의 차이"
    ),
    VOLUNTARY_COMMUNICATION_AND_RELATIONSHIP(
        ChallengerStatus.WITHDRAWN,
        "소통 및 관계의 어려움",
        "운영진·구성원과의 소통·협업 문제"
    ),
    VOLUNTARY_OTHER(ChallengerStatus.WITHDRAWN, "기타", "직접 입력"),

    // 타의 탈부
    INVOLUNTARY_POINT_THRESHOLD(
        ChallengerStatus.EXPELLED,
        "상벌점 합계 -20점",
        "상벌점 총점이 -20점 이하"
    ),
    INVOLUNTARY_EVENT_NO_SHOW(
        ChallengerStatus.EXPELLED,
        "행사 노쇼",
        "사전 연락 없이 행사 불참 (총칙 기준 횟수)"
    ),
    INVOLUNTARY_LOW_PROJECT_PARTICIPATION(
        ChallengerStatus.EXPELLED,
        "프로젝트 참여 저조",
        "TF 조사를 거친 경우"
    ),
    INVOLUNTARY_OTHER(ChallengerStatus.EXPELLED, "기타", "직접 입력");

    private final ChallengerStatus status;
    private final String displayName;
    private final String description;

    public boolean isOther() {
        return this == VOLUNTARY_OTHER || this == INVOLUNTARY_OTHER;
    }
}
