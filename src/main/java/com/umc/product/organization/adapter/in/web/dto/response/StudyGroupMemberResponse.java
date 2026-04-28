package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupMemberInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스터디 그룹 스터디원 응답")
public record StudyGroupMemberResponse(
    @Schema(description = "멤버 ID", example = "102") Long memberId,
    @Schema(description = "멤버 이름", example = "강하나") String memberName,
    @Schema(description = "소속 학교 ID", example = "15") Long schoolId,
    @Schema(description = "소속 학교명", example = "동국대학교") String schoolName,
    @Schema(description = "프로필 이미지 URL") String profileImageUrl
) {
    public static StudyGroupMemberResponse from(StudyGroupMemberInfo info) {
        return new StudyGroupMemberResponse(
            info.memberId(),
            info.memberName(),
            info.schoolId(),
            info.schoolName(),
            info.profileImageUrl()
        );
    }
}
