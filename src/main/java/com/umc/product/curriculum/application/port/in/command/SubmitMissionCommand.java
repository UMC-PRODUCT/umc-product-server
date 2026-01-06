package com.umc.product.curriculum.application.port.in.command;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public record SubmitMissionCommand(
        @NotNull(message = "미션 ID는 필수입니다")
        Long missionId,
        @NotNull(message = "미션 ID는 필수입니다")
        Long challengerWorkbookId,
        String submission
) {
    public SubmitMissionCommand {
        Objects.requireNonNull(missionId, "missionId must not be null");
        Objects.requireNonNull(challengerWorkbookId, "challengerWorkbookId must not be null");
        // submission은 PLAIN 타입일 때 null 가능
    }
}
