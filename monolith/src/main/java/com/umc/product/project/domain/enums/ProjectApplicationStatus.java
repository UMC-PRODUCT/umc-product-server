package com.umc.product.project.domain.enums;

public enum ProjectApplicationStatus {
    DRAFT,  // 아직 임시저장 상태로, 제출 이전임.
    SUBMITTED, // 지원서 제출 완료

    APPROVED, // 합격
    REJECTED, // 불합격
    CANCELLED // 지원자 본인이 철회 (soft delete). DRAFT/SUBMITTED에서만 전이 가능하며, 차수 OPEN 동안에만 허용.
}
