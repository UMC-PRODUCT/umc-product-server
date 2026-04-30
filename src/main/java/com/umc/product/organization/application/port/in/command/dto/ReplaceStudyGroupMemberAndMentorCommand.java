package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.Objects;
import java.util.Set;

public record ReplaceStudyGroupMemberAndMentorCommand(
    Long groupId,
    Set<Long> studyMemberIds,
    Set<Long> studyMentorIds
) {
    public ReplaceStudyGroupMemberAndMentorCommand {
        Objects.requireNonNull(groupId, "groupId must not be null");
        if (studyMemberIds == null || studyMemberIds.isEmpty()) {
            throw new OrganizationDomainException(OrganizationErrorCode.STUDY_GROUP_MEMBER_REQUIRED);
        }
        studyMemberIds = Set.copyOf(studyMemberIds);
    }

    public static ReplaceStudyGroupMemberAndMentorCommand of(
        Long groupId, Set<Long> studyMemberIds, Set<Long> studyMentorIds
    ) {
        return new ReplaceStudyGroupMemberAndMentorCommand(groupId, studyMemberIds, studyMentorIds);
    }
}
