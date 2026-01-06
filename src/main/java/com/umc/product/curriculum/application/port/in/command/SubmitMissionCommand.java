package com.umc.product.curriculum.application.port.in.command;

import java.util.Objects;

public record SubmitMissionCommand(
        Long missionId,
        Long challengerWorkbookId,
        String submission
) {
    public SubmitMissionCommand {
        Objects.requireNonNull(missionId, "missionId must not be null");
        Objects.requireNonNull(challengerWorkbookId, "challengerWorkbookId must not be null");
        // submission은 PLAIN 타입일 때 null 가능
    }
}
