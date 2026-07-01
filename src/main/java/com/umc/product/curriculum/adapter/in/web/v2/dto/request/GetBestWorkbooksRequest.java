package com.umc.product.curriculum.adapter.in.web.v2.dto.request;

import java.util.List;
import java.util.Set;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.dto.GetBestWorkbooksQuery;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GetBestWorkbooksRequest(
    @NotNull Long gisuId,
    Set<@Positive Long> schoolIds,
    Set<ChallengerPart> parts,
    List<@Positive Long> weekNos,
    List<@Positive Long> studyGroupIds,
    @Positive Long cursor,
    @Positive Integer size
) {

    public GetBestWorkbooksQuery toQuery() {
        return GetBestWorkbooksQuery.of(gisuId, schoolIds, parts, weekNos, studyGroupIds, cursor, size);
    }
}
