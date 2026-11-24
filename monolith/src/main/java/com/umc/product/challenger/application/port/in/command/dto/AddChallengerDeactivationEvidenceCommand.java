package com.umc.product.challenger.application.port.in.command.dto;

import java.util.List;

public record AddChallengerDeactivationEvidenceCommand(
    Long challengerId,
    List<String> imageIds,
    List<String> links,
    Long addedByMemberId
) {
    public AddChallengerDeactivationEvidenceCommand {
        imageIds = imageIds == null ? List.of() : List.copyOf(imageIds);
        links = links == null ? List.of() : List.copyOf(links);
    }

    public static AddChallengerDeactivationEvidenceCommand of(
        Long challengerId,
        List<String> imageIds,
        List<String> links,
        Long addedByMemberId
    ) {
        return new AddChallengerDeactivationEvidenceCommand(
            challengerId,
            imageIds,
            links,
            addedByMemberId
        );
    }
}
