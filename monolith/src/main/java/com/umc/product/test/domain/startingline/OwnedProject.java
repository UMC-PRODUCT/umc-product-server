package com.umc.product.test.domain.startingline;

import java.util.Map;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.domain.enums.ProjectStatus;

public record OwnedProject(
    Long projectId,
    Long ownerMemberId,
    ProjectStatus status,
    boolean hasName,
    boolean hasForm,
    Map<ChallengerPart, Integer> quotas
) {
    public OwnedProject {
        quotas = Map.copyOf(quotas);
    }

    public static OwnedProject newDraft(Long projectId, Long ownerMemberId) {
        return new OwnedProject(projectId, ownerMemberId, ProjectStatus.DRAFT, false, false, Map.of());
    }

    public OwnedProject withName() {
        return new OwnedProject(projectId, ownerMemberId, status, true, hasForm, quotas);
    }

    public OwnedProject withForm() {
        return new OwnedProject(projectId, ownerMemberId, status, hasName, true, quotas);
    }

    public OwnedProject withStatus(ProjectStatus newStatus) {
        return new OwnedProject(projectId, ownerMemberId, newStatus, hasName, hasForm, quotas);
    }

    public OwnedProject withQuotas(Map<ChallengerPart, Integer> newQuotas) {
        return new OwnedProject(projectId, ownerMemberId, status, hasName, hasForm, newQuotas);
    }
}
