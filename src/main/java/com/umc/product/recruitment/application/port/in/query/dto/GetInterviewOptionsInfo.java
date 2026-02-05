package com.umc.product.recruitment.application.port.in.query.dto;

import com.umc.product.recruitment.application.port.in.PartOption;
import java.time.LocalDate;
import java.util.List;

public record GetInterviewOptionsInfo(
        List<LocalDate> dates,
        List<PartOption> parts
) {
}