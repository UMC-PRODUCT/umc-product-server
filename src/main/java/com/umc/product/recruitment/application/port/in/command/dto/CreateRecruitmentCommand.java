package com.umc.product.recruitment.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

public record CreateRecruitmentCommand(
        String recruitmentName,
        List<ChallengerPart> parts
) {
}
