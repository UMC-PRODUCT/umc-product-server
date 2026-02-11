package com.umc.product.notice.adapter.in.web.swagger;

import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.response.PageResponse;
import com.umc.product.notice.adapter.in.web.dto.request.GetNoticeStatusRequest;
import com.umc.product.notice.adapter.in.web.dto.response.query.GetNoticeDetailResponse;
import com.umc.product.notice.adapter.in.web.dto.response.query.GetNoticeReadStatusResponse;
import com.umc.product.notice.adapter.in.web.dto.response.query.GetNoticeStaticsResponse;
import com.umc.product.notice.adapter.in.web.dto.response.query.GetNoticeSummaryResponse;
import com.umc.product.notice.dto.NoticeClassification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface NoticeQueryApi {

    @Operation(
        summary = "공지사항 전체 조회",
        description = "분류 필터별로 공지사항 목록을 페이징하여 조회합니다. "
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
        )
    })
    ApiResponse<PageResponse<GetNoticeSummaryResponse>> getAllNotices(
        @ParameterObject @Valid NoticeClassification classification,

        @Parameter(description = "페이징 정보. page=페이지 번호(0부터), size=페이지 크기, sort=정렬 기준(기본: createdAt,DESC)")
        @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    );

    @Operation(
        summary = "공지사항 검색",
        description = "키워드로 공지사항을 검색합니다. 제목과 내용에서 키워드를 검색하며, "
            + "분류 필터(classification)를 함께 사용하면 특정 범위 내에서만 검색됩니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "검색 성공"
        )
    })
    ApiResponse<PageResponse<GetNoticeSummaryResponse>> searchNotices(
        @Parameter(description = "검색 키워드. 공지 제목/내용에서 검색", required = true, example = "erica")
        @RequestParam String keyword,

        @ParameterObject @Valid NoticeClassification classification,

        @Parameter(description = "페이징 정보. page=페이지 번호(0부터), size=페이지 크기")
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
        description = "공지사항의 전체 대상자 수, 읽은 수, 안 읽은 수 통계를 조회합니다. "
    )
    ApiResponse<GetNoticeStaticsResponse> getNoticeReadStatics(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId
    );

    @Operation(
        summary = "공지사항 읽음 현황 상세 조회",
        description = "공지사항을 읽은/안읽은 사용자 목록을 조회합니다 (커서 기반 페이징). "
            + "status=READ이면 읽은 사람, UNREAD이면 안 읽은 사람을 조회합니다. "
            + "filterType으로 지부/학교별 필터링이 가능하며, 리마인더 발송 대상 선택에 활용할 수 있습니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        )
    })
    ApiResponse<CursorResponse<GetNoticeReadStatusResponse>> getNoticeReadStatus(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @ParameterObject @Valid GetNoticeStatusRequest request
    );
}
