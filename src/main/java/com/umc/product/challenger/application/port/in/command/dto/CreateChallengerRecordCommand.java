package com.umc.product.challenger.application.port.in.command.dto;

import com.umc.product.challenger.domain.ChallengerRecord;
import com.umc.product.common.domain.enums.ChallengerPart;
import lombok.Builder;

@Builder
public record CreateChallengerRecordCommand(
    Long creatorMemberId,
    Long gisuId,
    Long chapterId,
    Long schoolId,
    ChallengerPart part
) {
    @Override
    public String toString() {
        return "CreateChallengerRecordCommand{" +
            "creatorMemberId=" + creatorMemberId +
            ", gisuId=" + gisuId +
            ", chapterId=" + chapterId +
            ", schoolId=" + schoolId +
            ", part=" + part +
            '}';
    }

    public ChallengerRecord toEntity() {
        return ChallengerRecord.create(
            creatorMemberId, gisuId, chapterId, schoolId, part
        );
    }
}
