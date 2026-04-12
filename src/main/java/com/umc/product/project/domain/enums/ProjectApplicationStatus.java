package com.umc.product.project.domain.enums;

public enum ProjectApplicationStatus {
    PENDING,  // 아직 임시저장 상태로, 제출 이전임.
    SUBMITTED, // 지원서 제출 완료

    APPROVED, // 합격
    REJECTED, // 불합격

    CANCELED // 지원자가 지원을 취소한 경우
}
