package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

public enum MissionSubmissionStatusResponse {
    PASS, // 통과
    FAIL, // 제출했지만 평가에서 탈락한 경우 (벌점 부과 대상임)
    LATE, // 지각 제출한 경우 (벌점 부과 대상임)
    PENDING // 파트장 평가 대기 중

    // TODO: 도메인 메소드로 상태 결정 로직 작성해주세요
}
