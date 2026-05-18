package com.umc.product.organization.application.port.in.command.dto;

import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.Objects;
import java.util.Set;

public record CreateStudyGroupCommand(
        String name,
        Long gisuId,
        ChallengerPart part,
        Set<Long> mentorIds,
        Set<Long> memberIds
) {
    public CreateStudyGroupCommand {
        Objects.requireNonNull(name, "스터디 그룹 이름은 필수입니다.");
        Objects.requireNonNull(gisuId, "기수 ID는 필수입니다.");
        Objects.requireNonNull(part, "챌린저 파트는 필수입니다.");
        mentorIds = mentorIds != null ? Set.copyOf(mentorIds) : mentorIds;
        memberIds = memberIds != null ? Set.copyOf(memberIds) : memberIds;
    }
}
