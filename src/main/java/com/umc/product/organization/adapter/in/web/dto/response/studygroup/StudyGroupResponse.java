package com.umc.product.organization.adapter.in.web.dto.response.studygroup;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.organization.application.port.in.query.dto.studygroup.StudyGroupInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스터디 그룹 요약 정보")
public record StudyGroupResponse(
    @Schema(description = "스터디 그룹 ID", example = "1")
    Long studyGroupId,

    @Schema(description = "스터디 그룹명", example = "프로덕트팀 짱짱맨")
    String name,

    Long gisuId,

    ChallengerPart studyPart,

    @Schema(description = "파트장 목록")
    List<StudyGroupMemberResponse> mentors,

    @Schema(description = "멤버 목록")
    List<StudyGroupMemberResponse> members
) {

    public static StudyGroupResponse from(StudyGroupInfo info) {
        return new StudyGroupResponse(
            info.groupId(),
            info.name(),
            info.gisuId(),
            info.part(),
            info.mentors().stream().map(StudyGroupMemberResponse::from).toList(),
            info.members().stream().map(StudyGroupMemberResponse::from).toList());
    }
}
