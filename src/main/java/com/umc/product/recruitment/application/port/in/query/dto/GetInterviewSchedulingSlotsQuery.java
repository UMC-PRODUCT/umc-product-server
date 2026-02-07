package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.PartOption;
import java.time.LocalDate;

public record GetInterviewSchedulingSlotsQuery(
    Long recruitmentId,
    LocalDate date,
    PartOption part,
    Long requesterId
) {
}
