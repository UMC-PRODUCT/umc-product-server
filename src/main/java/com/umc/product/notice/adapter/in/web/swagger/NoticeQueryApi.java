package com.umc.product.notice.adapter.in.web.swagger;

import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.response.CursorResponse;
import com.umc.product.global.response.PageResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.notice.adapter.in.web.dto.request.GetNoticeStatusRequest;
import com.umc.product.notice.adapter.in.web.dto.response.query.GetNoticeDetailResponse;
import com.umc.product.notice.adapter.in.web.dto.response.query.GetNoticeReadStatusResponse;
import com.umc.product.notice.adapter.in.web.dto.response.query.GetNoticeStaticsResponse;
import com.umc.product.notice.adapter.in.web.dto.response.query.GetNoticeSummaryResponse;
import com.umc.product.notice.domain.NoticeClassification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Notice | 공지사항 Query", description = "")
public interface NoticeQueryApi {

    @Operation(
        summary = "공지사항 전체 조회",
        description = """
            공지사항 목록을 페이징하여 조회합니다.

            **minTargetRole 필드로 조회 대상을 명시해야 합니다.**
            - `minTargetRole=CHALLENGER` → 일반 챌린저 공지 조회
            - `minTargetRole=SCHOOL_PART_LEADER` 등 운영진 역할 → 해당 대상 운영진 공지 조회
              (조회자 역할이 요청 역할보다 낮으면 403 반환)

            **챌린저 공지 필터 (minTargetRole=CHALLENGER 일 때만 적용)**
            - `chapterId` 지정 → 해당 지부 공지만 조회
            - `schoolId` 지정 → 해당 학교 공지만 조회
            - `part` 지정 → 해당 파트 공지만 조회
              (chapterId/schoolId 미지정 시 조회자 소속 지부/학교로 자동 보완)

            **총괄단**은 임의의 chapterId/schoolId를 지정하여 다른 지부/학교 공지를 조회할 수 있습니다.
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "보유하지 않은 운영진 역할 요청"
        )
    })
    ApiResponse<PageResponse<GetNoticeSummaryResponse>> getAllNotices(
        @ParameterObject @Valid NoticeClassification classification,

        @Parameter(description = "페이징 정보. page=페이지 번호(0부터), size=페이지 크기, sort=정렬 기준(기본: createdAt,DESC)")
        @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable,

        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "공지사항 검색",
        description = """
            키워드로 공지사항을 검색합니다. 제목과 내용에서 검색합니다.

            **minTargetRole 필드 및 필터 조건은 전체 조회와 동일하게 적용됩니다.**
            """
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "검색 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "보유하지 않은 운영진 역할 요청"
        )
    })
    ApiResponse<PageResponse<GetNoticeSummaryResponse>> searchNotices(
        @Parameter(description = "검색 키워드. 공지 제목/내용에서 검색", required = true, example = "erica")
        @RequestParam String keyword,

        @ParameterObject @Valid NoticeClassification classification,

        @Parameter(description = "페이징 정보. page=페이지 번호(0부터), size=페이지 크기")
        @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable,

        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "공지사항 상세 조회",
        description = "특정 공지사항의 상세 정보를 조회합니다. READ 권한이 없으면 403을 반환합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "조회 권한 없음"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "공지사항을 찾을 수 없음"
        )
    })
    ApiResponse<GetNoticeDetailResponse> getNotice(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,
        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "공지사항 읽음 통계 조회",
        description = "공지사항의 전체 대상자 수, 읽은 수, 안 읽은 수 통계를 조회합니다."
    )
    ApiResponse<GetNoticeStaticsResponse> getNoticeReadStatics(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId
    );

    @Operation(
        summary = "공지사항 읽음 현황 상세 조회",
        description = """
            공지사항을 읽은/안읽은 사용자 목록을 커서 기반 페이징으로 조회합니다.

            - `status=READ` → 읽은 사람 목록
            - `status=UNREAD` → 안 읽은 사람 목록
            - `filterType`으로 지부/학교별 필터링 가능 (리마인더 발송 대상 선택에 활용)
            """
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
