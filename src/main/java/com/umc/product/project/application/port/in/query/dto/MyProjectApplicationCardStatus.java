package com.umc.product.project.application.port.in.query.dto;

import com.umc.product.project.domain.enums.ProjectApplicationStatus;

/**
 * 본인 지원 내역 카드 표시용 status enum.
 * <p>
 * 도메인 ENUM ({@link ProjectApplicationStatus}) 과 분리해, 향후 랜덤 매칭 등 표시용 라벨이 추가되어도 도메인 ENUM 의 시맨틱이 깨지지 않도록 한다.
 */
public enum MyProjectApplicationCardStatus {
    PENDING,
    SUBMITTED,
    APPROVED,
    REJECTED;

    public static MyProjectApplicationCardStatus from(ProjectApplicationStatus status) {
        return switch (status) {
            case PENDING -> PENDING;
            case SUBMITTED -> SUBMITTED;
            case APPROVED -> APPROVED;
            case REJECTED -> REJECTED;
        };
    }
}
