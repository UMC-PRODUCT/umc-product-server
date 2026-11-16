package com.umc.product.challenger.application.port.in.command.dto;

import java.util.List;

import com.umc.product.challenger.domain.enums.ChallengerDeactivationReason;

public record RegisterChallengerDeactivationCommand(
    Long challengerId,
    ChallengerDeactivationReason reason,
    String reasonDetail,
    String memo,
    List<String> imageIds,
    List<String> links,
    Long registeredByMemberId
) {
    public RegisterChallengerDeactivationCommand {
        imageIds = imageIds == null ? List.of() : List.copyOf(imageIds);
        links = links == null ? List.of() : List.copyOf(links);
    }

    public static RegisterChallengerDeactivationCommand of(
        Long challengerId,
        ChallengerDeactivationReason reason,
        String reasonDetail,
        String memo,
        List<String> imageIds,
        List<String> links,
        Long registeredByMemberId
    ) {
        return new RegisterChallengerDeactivationCommand(
            challengerId,
            reason,
            reasonDetail,
            memo,
            imageIds,
            links,
            registeredByMemberId
        );
    }
}
