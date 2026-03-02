package com.umc.product.challenger.application.port.in.command.dto;

import com.umc.product.challenger.domain.ChallengerRecord;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import lombok.Builder;

@Builder
public record CreateChallengerRecordCommand(
    Long creatorMemberId,
    Long gisuId,
    Long chapterId,
    Long schoolId,
    ChallengerPart part,
    String memberName,
    ChallengerRoleType challengerRoleType
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

    private boolean isAdminRecord() {
        return challengerRoleType != null;
    }

    public ChallengerRecord toEntity() {
        if (isAdminRecord()) {
            Long adminOrganizationId = switch (challengerRoleType.organizationType()) {
                case CENTRAL -> null; // 중앙운영사무국 소속은 organizationId가 필요없음
                case CHAPTER -> chapterId; // 챕터 관리자: organizationId는 chapterId
                case SCHOOL -> schoolId; // 학교 관리자: organizationId는 schoolId
            };

            return ChallengerRecord.createAdmin(
                creatorMemberId, gisuId, chapterId, schoolId, part, memberName,
                challengerRoleType, adminOrganizationId
            );
        } else {
            return ChallengerRecord.create(
                creatorMemberId, gisuId, chapterId, schoolId, part, memberName
            );
        }
    }
}
