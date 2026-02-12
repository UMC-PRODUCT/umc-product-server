package com.umc.product.organization.application.port.in.command.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record CreateChapterCommand(
    Long gisuId,
    String name,
    List<Long> schoolIds
) {
}
