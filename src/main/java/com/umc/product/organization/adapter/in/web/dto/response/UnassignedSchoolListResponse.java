package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.UnassignedSchoolInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "배정 대기 중인 학교 목록 응답")
public record UnassignedSchoolListResponse(
        @Schema(description = "배정 대기 중인 학교 목록")
        List<UnassignedSchoolItem> schools
) {

    public static UnassignedSchoolListResponse from(List<UnassignedSchoolInfo> infos) {
        List<UnassignedSchoolItem> schools = infos.stream()
                .map(UnassignedSchoolItem::from)
                .toList();
        return new UnassignedSchoolListResponse(schools);
    }

    @Schema(description = "학교 정보")
    public record UnassignedSchoolItem(
            @Schema(description = "학교 ID", example = "1")
            Long schoolId,
            @Schema(description = "학교명", example = "서울대학교")
            String schoolName
    ) {
        public static UnassignedSchoolItem from(UnassignedSchoolInfo info) {
            return new UnassignedSchoolItem(info.schoolId(), info.schoolName());
        }
    }
}
