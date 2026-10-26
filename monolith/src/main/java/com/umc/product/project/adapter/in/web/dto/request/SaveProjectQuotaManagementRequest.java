package com.umc.product.project.adapter.in.web.dto.request;

import java.util.List;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.command.dto.SaveProjectQuotaManagementCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SaveProjectQuotaManagementRequest(
    @NotNull Long gisuId,
    @NotNull Long chapterId,
    @NotEmpty @Size(max = 500) List<@NotNull @Valid ProjectChange> projects
) {
    public record ProjectChange(
        @NotNull Long projectId,
        @NotEmpty @Size(max = 10) List<@NotNull @Valid CellChange> entries
    ) {
    }
    public record CellChange(@NotNull ChallengerPart part, @NotNull @PositiveOrZero Long quota) {
    }

    public SaveProjectQuotaManagementCommand toCommand(Long requesterId) {
        return new SaveProjectQuotaManagementCommand(requesterId, gisuId, chapterId, projects.stream()
            .map(project -> new SaveProjectQuotaManagementCommand.ProjectChange(project.projectId(),
                project.entries().stream().map(entry -> new SaveProjectQuotaManagementCommand.CellChange(
                    entry.part(), entry.quota())).toList())).toList());
    }
}
