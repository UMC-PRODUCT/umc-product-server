package com.umc.product.notice.adapter.in.web;

import com.umc.product.common.dto.ChallengerContext;
import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.response.PageResponse;
import com.umc.product.notice.adapter.in.web.dto.request.GetNoticeFilterRequest;
import com.umc.product.notice.adapter.in.web.dto.response.GetNoticeDetailResponse;
import com.umc.product.notice.adapter.in.web.dto.response.GetNoticeReadStatusResponse;
import com.umc.product.notice.adapter.in.web.dto.response.GetNoticeSummaryResponse;
import com.umc.product.notice.adapter.in.web.dto.response.GetNoticesCategoryResponse;
import com.umc.product.notice.adapter.in.web.dto.response.GetNoticesScopeResponse;
import com.umc.product.notice.application.port.in.query.GetNoticeFilterUseCase;
import com.umc.product.notice.application.port.in.query.GetNoticeUseCase;
import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeScopeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeSummary;
import com.umc.product.notice.application.port.in.query.dto.WritableNoticeScopeOption;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Tag(name = Constants.NOTICE)
public class NoticeQueryController {

    private final GetNoticeUseCase getNoticeUseCase;
    private final GetNoticeFilterUseCase getNoticeFilterUseCase;

    /*
     * 공지 전체 조회
     */
    @GetMapping
    public ApiResponse<PageResponse<GetNoticeSummaryResponse>> getAllNotices(ChallengerContext context, GetNoticeFilterRequest request,
                                                   @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<NoticeSummary> notices = getNoticeUseCase.getAllNoticeSummaries(context, request.toInfo(),
                pageable);

        return ApiResponse.onSuccess(PageResponse.of(notices, GetNoticeSummaryResponse::from));
    }

    /*
     * 공지사항 상세 조회
     */
    @GetMapping("/{noticeId}")
    public ApiResponse<GetNoticeDetailResponse> getNotice(ChallengerContext context, @PathVariable Long noticeId) {
        NoticeInfo noticeDetail = getNoticeUseCase.getNoticeDetail(context, noticeId);
        return ApiResponse.onSuccess(GetNoticeDetailResponse.from(noticeDetail));
    }

    /*
     * 공지사항 수신 현황 조회
     */
    @GetMapping("/read-status")
    public ApiResponse<CursorResponse<GetNoticeReadStatusResponse>> getNoticeReadStatus() {

    }

    /*
     * 공지사항 전체 조회시 회원별 필터 조회
     */
    @GetMapping("/filters")
    public ApiResponse<GetNoticesScopeResponse> getNoticesScope(ChallengerContext context) {
        List<NoticeScopeInfo> filters = getNoticeFilterUseCase.getAvailableFilters(context);
        return ApiResponse.onSuccess(new GetNoticesScopeResponse(filters));
    }

    /*
     * 공지사항 작성 시 카테고리 조회
     */
    @GetMapping("/categories")
    public ApiResponse<GetNoticesCategoryResponse> getNoticesCategory(ChallengerContext context) {
        WritableNoticeScopeOption category = getNoticeFilterUseCase.getWritableNoticeScope(context);
        return ApiResponse.onSuccess(GetNoticesCategoryResponse.from(category));
    }
}
