package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import java.util.Set;

public record GetBestWorkbooksRequest(
    Long gisuId,
    Set<Long> schoolIds,
    Set<ChallengerPart> parts,
    List<Long> weekNos,
    List<Long> studyGroupIds
) {
}
