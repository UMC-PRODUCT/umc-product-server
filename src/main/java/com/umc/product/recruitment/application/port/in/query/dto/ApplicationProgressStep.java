package com.umc.product.recruitment.application.port.in.query.dto;

public enum ApplicationProgressStep {
    RECRUITMENT_UPCOMING, // 운영진 대시보드에 사용
    APPLY_OPEN, // 운영진 대시보드에 사용
    BEFORE_APPLY, // 지원자 대시보드에서만 사용
    DOC_REVIEWING,
    DOC_RESULT_PUBLISHED,
    INTERVIEW_WAITING,
    FINAL_REVIEWING,
    FINAL_RESULT_PUBLISHED
}
