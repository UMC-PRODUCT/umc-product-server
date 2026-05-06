package com.umc.product.notice.adapter.in.web.swagger;

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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
        summary = "[NOTICE-001] 공지사항 전체 조회",
        description = """
            `noticeTab` 값으로 챌린저 공지(`CHALLENGER`)와 운영진 공지(`CHALLENGER` 외)를 구분
            CHALLENGER 공지는 운영진 공지가 아닌 일반공지를 의미함. 운영진 공지가 아닌 이상 `noticeTab`은 항상 `CHALLENGER`로 고정되어야 함.
            운영진 공지를 조회할 때에는 조회자의 ROLE에 따라 CENTRAL_MEMBER, SCHOOL_CORE, SCHOOL_PART_LEADER 중 하나로 요청해야 하며, 이 값에 따라 조회 가능한 공지의 범위가 달라짐.
            - CHALLENGER -> 챌린저 공지
            - CENTRAL_MEMBER -> 중앙운영사무국 공지
            - SCHOOL_CORE -> 학교회장단 공지
            - SCHOOL_PART_LEADER -> 파트장 공지

            자기 role보다 상위 tab 요청시 오류 (403)


            운영진 공지는 중앙운영사무국 공지 / 교내 운영진 공지 이렇게 나뉘고, 기준은 `schoolId` 파라미터의 입력 여부
            (gisuId: O / chapterId: X / schoolId: 분류기준이 됨)

            - schoolId 입력 -> 교내 운영진 공지로 구분됨, 교내 회장단이 작성 가능
                - schoolId 입력 + noticeTab은 SCHOOL_PART_LEADER로 입력 → 해당 학교 모든 파트장 대상 공지
                - schoolId 입력 + noticeTab은 SCHOOL_PART_LEADER로 입력 + targetParts 특정 파트 지정 → 해당 학교 특정 파트장 대상 공지

            - schoolId 미입력 -> 중앙운영사무국 공지로 구분됨, 총괄단과 중앙운영진이 작성 가능
                - schoolId 미입력 + noticeTab이 CENTRAL_MEMBER인 경우 → 중앙운영진 대상 공지, 총괄단 작성 가능
                - schoolId 미입력 + noticeTab이 SCHOOL_CORE인 경우 → 학교회장단 대상 공지, 총괄단과 중앙운영진 작성 가능
                - schoolId 미입력 + noticeTab이 SCHOOL_PART_LEADER인 경우 → 학교 파트장 대상 공지, 총괄단과 중앙운영진 작성 가능
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PageResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "보유하지 않은 운영진 역할 요청"
        )
    })
    PageResponse<GetNoticeSummaryResponse> getAllNotices(
        @ParameterObject @Valid NoticeClassification classification,

        @Parameter(description = "페이징 정보. page=페이지 번호(0부터), size=페이지 크기, sort=정렬 기준(기본: createdAt,DESC)")
        @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable,

        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "[NOTICE-002] 공지사항 검색",
        description = """
            키워드로 공지사항을 검색합니다. 제목과 내용에서 검색합니다.

            **targetNoticeTab 필드 및 필터 조건은 전체 조회와 동일하게 적용됩니다.**
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "검색 성공"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "보유하지 않은 운영진 역할 요청"
        )
    })
    PageResponse<GetNoticeSummaryResponse> searchNotices(
        @Parameter(description = "검색 키워드. 공지 제목/내용에서 검색", required = true, example = "erica")
        @RequestParam String keyword,

        @ParameterObject @Valid NoticeClassification classification,

        @Parameter(description = "페이징 정보. page=페이지 번호(0부터), size=페이지 크기")
        @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable,

        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "[NOTICE-003] 공지사항 상세 조회",
        description = "특정 공지사항의 상세 정보를 조회합니다. READ 권한이 없으면 403을 반환합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "조회 권한 없음"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "공지사항을 찾을 수 없음"
        )
    })
    GetNoticeDetailResponse getNotice(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,
        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "[NOTICE-004] 공지사항 읽음 통계 조회",
        description = "공지사항의 전체 대상자 수, 읽은 수, 안 읽은 수 통계를 조회합니다."
    )
    GetNoticeStaticsResponse getNoticeReadStatics(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId
    );

    @Operation(
        summary = "[NOTICE-005] 공지사항 읽음 현황 상세 조회",
        description = """
            공지사항을 읽은/안읽은 사용자 목록을 커서 기반 페이징으로 조회합니다.

            - `status=READ` → 읽은 사람 목록
            - `status=UNREAD` → 안 읽은 사람 목록
            - `filterType`으로 지부/학교별 필터링 가능 (리마인더 발송 대상 선택에 활용)
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        )
    })
    CursorResponse<GetNoticeReadStatusResponse> getNoticeReadStatus(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @ParameterObject @Valid GetNoticeStatusRequest request
    );
}
