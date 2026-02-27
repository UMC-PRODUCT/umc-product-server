package com.umc.product.recruitment.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

public record CreateRecruitmentRequest(
    String recruitmentName,
    List<ChallengerPart> parts
) {
    public static CreateRecruitmentRequest empty() {
        return new CreateRecruitmentRequest(null, null);
    }
}
