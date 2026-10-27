package com.umc.product.project.application.port.in.command;

import com.umc.product.project.domain.enums.MatchingType;

public interface DeleteProjectMatchingRoundUseCase {

    void delete(Long matchingRoundId, Long requesterMemberId);

    void deleteAll(Long chapterId, MatchingType type, Long requesterMemberId);
}
