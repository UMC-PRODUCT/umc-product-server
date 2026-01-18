package com.umc.product.recruitment.adapter.in.web.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

public record CreateDraftRecruitmentRequest(
        String recruitmentName,
        List<ChallengerPart> parts
) {
    public static CreateDraftRecruitmentRequest empty() {
        return new CreateDraftRecruitmentRequest(null, null);
    }
}
