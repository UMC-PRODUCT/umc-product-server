package com.umc.product.curriculum.adapter.in.web.v2.dto.response;

import com.umc.product.curriculum.domain.enums.SubmissionStatus;

public enum MissionSubmissionStatusResponse {
    PASS,
    FAIL,
    LATE,
    PENDING;

    public static MissionSubmissionStatusResponse from(SubmissionStatus status) {
        return valueOf(status.name());
    }
}