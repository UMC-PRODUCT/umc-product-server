package com.umc.product.organization.application.port.in.query.dto;

import java.time.LocalDate;

public record GisuInfo(Long gisuId, Long number, LocalDate startAt, LocalDate endAt, boolean isActive) {
}
