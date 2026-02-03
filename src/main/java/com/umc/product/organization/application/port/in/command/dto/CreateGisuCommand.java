package com.umc.product.organization.application.port.in.command.dto;

import java.time.Instant;

public record CreateGisuCommand(Long number, Instant startAt, Instant endAt) {
}
