package com.umc.product.recruitment.application.port.in.command;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

public record CreateExtensionCommand(
    Long memberId,
    Long baseRecruitmentId,
    String recruitmentName,
    List<ChallengerPart> parts
) {
}
