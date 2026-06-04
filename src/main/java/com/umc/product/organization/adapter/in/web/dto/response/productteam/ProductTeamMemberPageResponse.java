package com.umc.product.organization.adapter.in.web.dto.response.productteam;

import com.umc.product.global.response.PageResponse;
import java.util.List;

public record ProductTeamMemberPageResponse(
    List<ProductTeamMemberResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {
    public static ProductTeamMemberPageResponse from(PageResponse<ProductTeamMemberResponse> pageResponse) {
        return new ProductTeamMemberPageResponse(
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
