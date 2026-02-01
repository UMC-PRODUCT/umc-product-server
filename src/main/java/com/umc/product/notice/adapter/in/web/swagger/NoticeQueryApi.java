package com.umc.product.notice.adapter.in.web.swagger;

import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.response.PageResponse;
import com.umc.product.notice.adapter.in.web.dto.request.GetNoticeStatusRequest;
import com.umc.product.notice.adapter.in.web.dto.response.query.GetNoticeDetailResponse;
import com.umc.product.notice.adapter.in.web.dto.response.query.GetNoticeReadStatusResponse;
import com.umc.product.notice.adapter.in.web.dto.response.query.GetNoticeStaticsResponse;
import com.umc.product.notice.adapter.in.web.dto.response.query.GetNoticeSummaryResponse;
import com.umc.product.notice.domain.enums.NoticeClassification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface NoticeQueryApi {

    @Operation(
        summary = "공지사항 전체 조회",
        description = "분류별로 공지사항 목록을 페이징하여 조회합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
        )
    })
    ApiResponse<PageResponse<GetNoticeSummaryResponse>> getAllNotices(
        @Parameter(description = "공지 분류 (GENERAL, IMPORTANT 등)", required = true)
        @RequestParam NoticeClassification classification,

        @Parameter(description = "페이징 정보 (page, size, sort)")
        @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    );

    @Operation(
        summary = "공지사항 검색",
        description = "키워드로 공지사항을 검색합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "검색 성공"
        )
    })
    ApiResponse<PageResponse<GetNoticeSummaryResponse>> searchNotices(
        @Parameter(description = "검색 키워드", required = true, example = "회의")
        @RequestParam String keyword,

        @Parameter(description = "페이징 정보")
        @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    );

    @Operation(
        summary = "공지사항 상세 조회",
        description = "특정 공지사항의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "공지사항을 찾을 수 없음"
        )
    })
    ApiResponse<GetNoticeDetailResponse> getNotice(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId
    );

    @Operation(
        summary = "공지사항 읽음 통계 조회",
        description = "공지사항의 읽음/안읽음 통계를 조회합니다."
    )
    ApiResponse<GetNoticeStaticsResponse> getNoticeReadStatics(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId
    );

    @Operation(
        summary = "공지사항 읽음 현황 상세 조회",
        description = "공지사항을 읽은/안읽은 사용자 목록을 필터링하여 조회합니다. (커서 기반 페이징)"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        )
    })
    ApiResponse<CursorResponse<GetNoticeReadStatusResponse>> getNoticeReadStatus(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(description = "읽음 현황 필터 조건")
        @ModelAttribute @Valid GetNoticeStatusRequest request
    );
}
