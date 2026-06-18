package com.umc.product.organization.application.port.in.query.dto.studygroup;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.time.Instant;
import java.util.List;

public record StudyGroupWithMemberAndMentorInfo(
    Long groupId,
    String name,
    Long gisuId,
    ChallengerPart part,
    Instant createdAt,
    List<StudyGroupMemberInfo> mentors,
    List<StudyGroupMemberInfo> members
) {
    public StudyGroupWithMemberAndMentorInfo {
        mentors = mentors == null ? List.of() : List.copyOf(mentors);
        members = members == null ? List.of() : List.copyOf(members);
    }

    public static StudyGroupWithMemberAndMentorInfo create(
        Long groupId, String name,
        Long gisuId, ChallengerPart part,
        Instant createdAt,
        List<StudyGroupMemberInfo> mentors, List<StudyGroupMemberInfo> members
    ) {
        return new StudyGroupWithMemberAndMentorInfo(
            groupId, name,
            gisuId, part,
            createdAt,
            mentors, members
        );
    }
}
