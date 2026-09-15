package com.umc.product.project.application.port.in.command;

public interface DeleteProjectMatchingRoundUseCase {

    void delete(Long matchingRoundId, Long requesterMemberId);
}
