package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.Objects;
import java.util.Set;

public record AddStudyGroupMembersCommand(
        Long groupId,
        Set<Long> memberIds
) {
    public AddStudyGroupMembersCommand {
        Objects.requireNonNull(groupId, "groupId must not be null");
        if (memberIds == null || memberIds.isEmpty()) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MEMBER_REQUIRED);
        }
        memberIds = Set.copyOf(memberIds);
    }
}
