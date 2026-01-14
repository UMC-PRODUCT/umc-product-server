package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.domain.ApplicationStatus;
import java.util.List;

public record MyApplicationListInfo(
        List<MyApplicationSummary> applications
) {

    public record MyApplicationSummary(
            Long applicationId,
            Long recruitmentId,
            String recruitmentTitle,
            ApplicationStatus status
    ) {
    }

}
