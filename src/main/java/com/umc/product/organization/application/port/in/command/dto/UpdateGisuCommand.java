package com.umc.product.organization.application.port.in.command.dto;

import java.time.Instant;

public record UpdateGisuCommand(Long gisuId, Instant startAt, Instant endAt) {
}