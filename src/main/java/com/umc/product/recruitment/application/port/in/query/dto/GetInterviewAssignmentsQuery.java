package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.PartOption;
import java.time.LocalDate;

public record GetInterviewAssignmentsQuery(
        Long recruitmentId,
        LocalDate date,
        PartOption part,
        Long memberId
) {
}
