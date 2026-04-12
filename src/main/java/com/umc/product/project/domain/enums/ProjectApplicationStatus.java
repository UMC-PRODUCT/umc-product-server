package com.umc.product.project.domain.enums;

public enum ProjectApplicationStatus {
    PENDING, // 지원서 제출 후 접수 완료

    PASSED, // 합격
    REJECTED, // 불합격

    CANCELED // 지원자가 지원을 취소한 경우
}
