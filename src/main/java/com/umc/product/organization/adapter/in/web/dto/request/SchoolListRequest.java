package com.umc.product.organization.adapter.in.web.dto.request;

import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학교 목록 검색 조건")
public record SchoolListRequest(
        @Schema(description = "검색 키워드 (학교명)", example = "서울")
        String keyword,

        @Schema(description = "지부 ID (필터링)", example = "1")
        Long chapterId
) {

    public SchoolSearchCondition toCondition() {
        return new SchoolSearchCondition(
                keyword,
                chapterId
        );
    }
}

