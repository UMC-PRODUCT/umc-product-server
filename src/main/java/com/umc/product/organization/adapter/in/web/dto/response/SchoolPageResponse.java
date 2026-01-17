package com.umc.product.organization.adapter.in.web.dto.response;

import com.umc.product.global.response.PageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "학교 목록 페이징 응답")
public record SchoolPageResponse(
        @Schema(description = "학교 목록")
        List<SchoolListItemResponse> content,

        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        int page,

        @Schema(description = "페이지 크기", example = "20")
        int size,

        @Schema(description = "전체 항목 수", example = "100")
        long totalElements,

        @Schema(description = "전체 페이지 수", example = "5")
        int totalPages,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "이전 페이지 존재 여부", example = "false")
        boolean hasPrevious
) {
    public static SchoolPageResponse from(PageResponse<SchoolListItemResponse> pageResponse) {
        return new SchoolPageResponse(
                pageResponse.content(),
                pageResponse.page(),
                pageResponse.size(),
                pageResponse.totalElements(),
                pageResponse.totalPages(),
                pageResponse.hasNext(),
                pageResponse.hasPrevious()
        );
    }
}
