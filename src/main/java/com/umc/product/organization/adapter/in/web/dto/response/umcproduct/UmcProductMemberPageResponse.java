package com.umc.product.organization.adapter.in.web.dto.response.umcproduct;

import java.util.List;

import com.umc.product.global.response.PageResponse;

public record UmcProductMemberPageResponse(
    List<UmcProductMemberResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {
    public static UmcProductMemberPageResponse from(PageResponse<UmcProductMemberResponse> pageResponse) {
        return new UmcProductMemberPageResponse(
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
