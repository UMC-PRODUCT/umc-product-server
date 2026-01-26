package com.umc.product.notice.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.response.PageResponse;
import com.umc.product.notice.adapter.in.web.dto.request.GetNoticeStatusRequest;
import com.umc.product.notice.adapter.in.web.dto.response.GetNoticeDetailResponse;
import com.umc.product.notice.adapter.in.web.dto.response.GetNoticeReadStatusResponse;
import com.umc.product.notice.adapter.in.web.dto.response.GetNoticeStaticsResponse;
import com.umc.product.notice.adapter.in.web.dto.response.GetNoticeSummaryResponse;
import com.umc.product.notice.adapter.in.web.swagger.NoticeQueryApi;
import com.umc.product.notice.application.port.in.query.GetNoticeUseCase;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusResult;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusSummary;
import com.umc.product.notice.application.port.in.query.dto.NoticeSummary;
import com.umc.product.notice.domain.enums.NoticeClassification;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Tag(name = Constants.NOTICE)
public class NoticeQueryController implements NoticeQueryApi {

    private final GetNoticeUseCase getNoticeUseCase;

    /*
     * 공지 전체 조회
     */
    @GetMapping
    public ApiResponse<PageResponse<GetNoticeSummaryResponse>> getAllNotices(@RequestParam NoticeClassification classification,
                                                   @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NoticeSummary> notices = getNoticeUseCase.getAllNoticeSummaries(classification,
                pageable);

        return ApiResponse.onSuccess(PageResponse.of(notices, GetNoticeSummaryResponse::from));
    }

    /*
     * 검색어 기반 공지 전체 조회
     */
    @GetMapping("/search")
    public ApiResponse<PageResponse<GetNoticeSummaryResponse>>  searchNotices(@RequestParam String keyword,
                                                   @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NoticeSummary> notices = getNoticeUseCase.searchNoticesByKeyword(keyword, pageable);

        return ApiResponse.onSuccess(PageResponse.of(notices, GetNoticeSummaryResponse::from));
    }

    /*
     * 공지사항 상세 조회
     */
    @GetMapping("/{noticeId}")
    public ApiResponse<GetNoticeDetailResponse> getNotice(@PathVariable Long noticeId) {
        NoticeInfo noticeDetail = getNoticeUseCase.getNoticeDetail(noticeId);
        return ApiResponse.onSuccess(GetNoticeDetailResponse.from(noticeDetail));
    }

    /*
     * 공지사항 수신 현황 통계 조회
     */
    @GetMapping("/{noticeId}/read-statics")
    public ApiResponse<GetNoticeStaticsResponse> getNoticeReadStatics(@PathVariable Long noticeId) {
        NoticeReadStatusSummary statistics = getNoticeUseCase.getReadStatistics(noticeId);
        return ApiResponse.onSuccess(GetNoticeStaticsResponse.from(statistics));
    }

    /*
     * 공지사항 수신 현황 조회
     */
    @GetMapping("/{noticeId}/read-status")
    public ApiResponse<CursorResponse<GetNoticeReadStatusResponse>> getNoticeReadStatus(@PathVariable Long noticeId, @ModelAttribute @Valid GetNoticeStatusRequest request) {
        NoticeReadStatusResult result = getNoticeUseCase.getReadStatus(request.toQuery(noticeId));
        CursorResponse<GetNoticeReadStatusResponse> response = CursorResponse.of(
                result.content().stream()
                        .map(GetNoticeReadStatusResponse::from)
                        .toList(),
                result.cursorId(),
                result.hasNext()
        );

        return ApiResponse.onSuccess(response);
    }
}
