package com.umc.product.organization.application.port.in.query.dto.studygroup;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.domain.StudyGroup;
import com.umc.product.organization.domain.StudyGroupMember;
import com.umc.product.organization.domain.StudyGroupMentor;
import java.time.Instant;
import java.util.List;

public record StudyGroupInfo(
    Long groupId,
    String name,
    Long gisuId,
    ChallengerPart part,
    Instant createdAt,
    List<Long> mentorIds,
    List<Long> memberIds
) {
    public StudyGroupInfo {
        mentorIds = mentorIds == null ? List.of() : List.copyOf(mentorIds);
        memberIds = memberIds == null ? List.of() : List.copyOf(memberIds);
    }

    public static StudyGroupInfo create(
        Long groupId, String name,
        Long gisuId, ChallengerPart part,
        Instant createdAt,
        List<Long> mentorIds, List<Long> memberIds
    ) {
        return new StudyGroupInfo(
            groupId, name,
            gisuId, part,
            createdAt,
            mentorIds, memberIds
        );
    }

    public static StudyGroupInfo from(StudyGroup studyGroup) {
        return new StudyGroupInfo(
            studyGroup.getId(),
            studyGroup.getName(),
            studyGroup.getGisuId(),
            studyGroup.getPart(),
            studyGroup.getCreatedAt(),
            studyGroup.getMentors().stream()
                .map(StudyGroupMentor::getMemberId)
                .toList(),
            studyGroup.getMembers().stream()
                .map(StudyGroupMember::getMemberId)
                .toList()
        );
    }
}
