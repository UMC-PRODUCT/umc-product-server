package com.umc.product.organization.application.port.in.command.dto;

import java.time.LocalDate;

public record CreateGisuCommand(Long number, LocalDate startAt, LocalDate endAt) {
}
