package com.umc.product.recruitment.application.port.in.query;

public enum RecruitmentListStatus {
    UPCOMING,
    ONGOING,
    CLOSED,
    DRAFT;

    public static RecruitmentListStatus fromRequest(String value) {
        return switch (value) {
            case "SCHEDULED" -> UPCOMING;
            default -> RecruitmentListStatus.valueOf(value);
        };
    }
}

