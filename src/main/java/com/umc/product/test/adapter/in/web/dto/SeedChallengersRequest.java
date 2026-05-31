package com.umc.product.test.adapter.in.web.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.test.application.port.in.command.dto.SeedChallengersCommand;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record SeedChallengersRequest(
    Long gisuId,
    @Positive
    int countPerPartPerSchool,
    List<ChallengerPart> parts,
    List<Long> chapterIds
) {

    public SeedChallengersCommand toCommand() {
        return new SeedChallengersCommand(gisuId, countPerPartPerSchool, parts, chapterIds);
    }
}
