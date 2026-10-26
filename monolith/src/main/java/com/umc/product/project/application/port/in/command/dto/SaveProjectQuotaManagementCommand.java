package com.umc.product.project.application.port.in.command.dto;

import java.util.List;
import java.util.Objects;

import com.umc.product.common.domain.enums.ChallengerPart;

public record SaveProjectQuotaManagementCommand(
    Long requesterId, Long gisuId, Long chapterId, List<ProjectChange> projects
) {
    public SaveProjectQuotaManagementCommand {
        Objects.requireNonNull(requesterId);
        Objects.requireNonNull(gisuId);
        Objects.requireNonNull(chapterId);
        projects = List.copyOf(projects);
    }
    public record ProjectChange(Long projectId, List<CellChange> entries) {
        public ProjectChange {
            Objects.requireNonNull(projectId);
            entries = List.copyOf(entries);
        }
    }
    public record CellChange(ChallengerPart part, Long quota) {
        public CellChange {
            Objects.requireNonNull(part);
            Objects.requireNonNull(quota);
        }
    }
}
