package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.organization.application.port.in.query.dto.SchoolNameInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "학교 전체 목록 응답")
public record SchoolNameListResponse(
        @Schema(description = "학교 목록")
        List<SchoolNameItem> schools
) {
    public static SchoolNameListResponse from(List<SchoolNameInfo> infos) {
        List<SchoolNameItem> schools = infos.stream()
                .map(SchoolNameItem::from)
                .toList();
        return new SchoolNameListResponse(schools);
    }

    @Schema(description = "학교 정보")
    public record SchoolNameItem(
            @Schema(description = "학교 ID", example = "1")
            Long schoolId,
            @Schema(description = "학교명", example = "서울대학교")
            String schoolName
    ) {
        public static SchoolNameItem from(SchoolNameInfo info) {
            return new SchoolNameItem(info.schoolId(), info.schoolName());
        }
    }
}
