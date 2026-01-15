package com.umc.product.organization.application.port.in.command.dto;

import java.time.LocalDate;

public record UpdateGisuCommand(Long gisuId, LocalDate startAt, LocalDate endAt) {
}