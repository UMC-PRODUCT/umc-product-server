package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.test.application.port.in.command.dto.SeedNoticeCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record SeedNoticeRequest(
    Long gisuId,
    @NotNull
    Long authorMemberId,
    @PositiveOrZero
    int globalCount,
    @PositiveOrZero
    int perChapterCount,
    @PositiveOrZero
    int perSchoolCount,
    @PositiveOrZero
    int perPartCount,
    List<ChallengerPart> parts
) {

    public SeedNoticeCommand toCommand() {
        return new SeedNoticeCommand(
            gisuId, authorMemberId,
            globalCount, perChapterCount, perSchoolCount, perPartCount,
            parts
        );
    }
}
