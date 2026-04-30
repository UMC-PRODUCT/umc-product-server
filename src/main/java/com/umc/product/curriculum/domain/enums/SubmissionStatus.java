package com.umc.product.curriculum.domain.enums;

public enum SubmissionStatus {
    PENDING, // 파트장 평가 대기 중
    PASS,    // 통과
    FAIL,    // 평가 탈락 (벌점 부과 대상)
    LATE,    // 지각 제출 (벌점 부과 대상)
}