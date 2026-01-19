package com.umc.product.curriculum.adapter.in.web.dto.response;

import com.umc.product.curriculum.application.port.in.query.dto.StudyGroupFilterInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "필터용 스터디 그룹 정보")
public record StudyGroupFilterResponse(
        @Schema(description = "스터디 그룹 ID", example = "1")
        Long groupId,

        @Schema(description = "스터디 그룹 이름", example = "스프링 1조")
        String name
) {
    public static StudyGroupFilterResponse from(StudyGroupFilterInfo info) {
        return new StudyGroupFilterResponse(info.groupId(), info.name());
    }
}
