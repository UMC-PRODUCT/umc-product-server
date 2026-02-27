package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.StudyGroupNameInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스터디 그룹 이름 목록 응답")
public record StudyGroupNameResponse(
        @Schema(description = "스터디 그룹 목록")
        List<StudyGroupName> studyGroups
) {
    @Schema(description = "스터디 그룹 이름 정보")
    public record StudyGroupName(
            @Schema(description = "스터디 그룹 ID", example = "1")
            Long groupId,
            @Schema(description = "스터디 그룹명", example = "React A팀")
            String name
    ) {
        public static StudyGroupName from(StudyGroupNameInfo info) {
            return new StudyGroupName(info.groupId(), info.name());
        }
    }

    public static StudyGroupNameResponse from(List<StudyGroupNameInfo> infos) {
        return new StudyGroupNameResponse(
                infos.stream().map(StudyGroupName::from).toList()
        );
    }
}
