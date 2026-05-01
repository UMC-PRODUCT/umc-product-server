package com.umc.product.organization.application.port.in.query.dto.studygroup;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;

public record StudyGroupInfo(
    Long groupId,
    String name,
    Long gisuId,
    ChallengerPart part,
    List<StudyGroupMemberInfo> mentors,
    List<StudyGroupMemberInfo> members
) {
    public static StudyGroupInfo create(
        Long groupId, String name,
        Long gisuId, ChallengerPart part,
        List<StudyGroupMemberInfo> mentors, List<StudyGroupMemberInfo> members
    ) {
        return new StudyGroupInfo(
            groupId, name,
            gisuId, part,
            mentors, members
        );
    }
}
