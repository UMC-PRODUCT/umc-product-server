package com.umc.product.notice.adapter.in.web.swagger;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Notice | 공지사항 Query", description = "")
public interface NoticeQueryApi {

    @Operation(
        operationId = "NOTICE-001",
        summary = "공지사항 전체 조회",
        description = """
            `noticeTab` 값으로 챌린저 공지(`CHALLENGER`)와 운영진 공지(`CHALLENGER` 외)를 구분
            CHALLENGER 공지는 운영진 공지가 아닌 일반공지를 의미함. 운영진 공지가 아닌 이상 `noticeTab`은 항상 `CHALLENGER`로 고정되어야 함.
            운영진 공지를 조회할 때에는 조회자의 ROLE에 따라 CENTRAL_MEMBER, SCHOOL_CORE, SCHOOL_PART_LEADER 중 하나로 요청합니다.
            이 값에 따라 조회 가능한 공지의 범위가 달라집니다.
            - CHALLENGER -> 챌린저 공지
            - CENTRAL_MEMBER -> 중앙운영사무국 공지
            - SCHOOL_CORE -> 학교회장단 공지
            - SCHOOL_PART_LEADER -> 파트장 공지

            자기 role보다 상위 tab 요청시 오류 (403)


            ### 조회 범위

            - `chapterId`, `schoolId`, `part`를 모두 생략하면 'UMC 전체'로 조회하며,
              조회자의 권한 내 전체·지부·학교 공지를 함께 반환합니다. 일반 챌린저와 파트장은 본인 파트 기준을 유지합니다.
            - 운영진 공지에서도 학교 필터를 생략하면 열람 가능한 중앙·학교 운영진 공지를 함께 반환합니다.
            - 지부장은 학교를 지정하지 않은 중앙 발신 운영진 공지만 열람할 수 있습니다.
              `SCHOOL_CORE`와 `SCHOOL_PART_LEADER` 대상 공지는 열람 가능하며, `CENTRAL_MEMBER` 대상은 열람할 수 없습니다.
              학교 운영진 공지 조회 요청은 403을 반환합니다.
            - 운영진 공지는 `gisuId`가 필수이며 `chapterId`는 지정할 수 없습니다.

            ### 작성자 표시

            `author`는 작성자의 `memberId`, `name`, `nickname`과 현재 활성 기수의
            `gisuId`, `roleType`, `roleName`, `organizationType`, `organizationId`, `organizationName`을 반환합니다.
            역할이 여러 개이면 가장 높은 직책을 표시합니다. 활성 기수의 역할이 없으면 역할·소속은 null입니다.
            기존 `authorMemberId`, `authorName`, `authorNickname`, `authorChallengerId`는 유지합니다.

            ### 첨부 배지 정보

            각 공지 항목은 다음 첨부 존재 여부를 반환합니다. 첨부가 없으면 false입니다.
            - `hasImages`: 이미지 첨부 존재 여부
            - `hasLinks`: 링크 첨부 존재 여부
            - `hasVote`: 투표 첨부 존재 여부. 투표 시작 전이나 종료 후에도 첨부가 있으면 true입니다.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공"
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
        operationId = "NOTICE-002",
        summary = "공지사항 검색",
        description = """
            키워드로 공지사항을 검색합니다. 제목과 내용에서 검색합니다.

            **targetNoticeTab 필드 및 필터 조건은 전체 조회와 동일하게 적용됩니다.**

            'UMC 전체' 조회 범위와 `author`의 활성 기수 최상위 역할·소속 표시도 목록 조회와 동일합니다.

            ### 첨부 배지 정보

            각 공지 항목은 목록 조회와 동일한 첨부 존재 여부를 반환합니다. 첨부가 없으면 false입니다.
            - `hasImages`: 이미지 첨부 존재 여부
            - `hasLinks`: 링크 첨부 존재 여부
            - `hasVote`: 투표 첨부 존재 여부. 투표 시작 전이나 종료 후에도 첨부가 있으면 true입니다.
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
        operationId = "NOTICE-003",
        summary = "공지사항 상세 조회",
        description = """
            특정 공지사항의 상세 정보를 조회합니다. READ 권한이 없으면 403을 반환합니다.

            ### 수정 일시

            - `updatedAt`: 공지 수정 일시. ISO 8601 UTC 형식으로 반환합니다.
            - 제목·본문·필독 여부가 변경되거나 이미지·링크·투표 추가·교체·삭제가 처리되면 갱신합니다.
            - 상세 조회와 조회수 증가는 수정 일시를 변경하지 않습니다.

            ### 작성자 표시

            `author`는 목록과 동일하게 작성자의 이름·닉네임 및 현재 활성 기수의 최상위 역할·소속을 반환합니다.
            활성 기수의 역할이 없으면 역할·소속은 null입니다.
            """
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
        operationId = "NOTICE-004",
        summary = "공지사항 읽음 통계 조회",
        description = "공지사항의 전체 대상자 수, 읽은 수, 안 읽은 수 통계를 조회합니다."
    )
    GetNoticeStaticsResponse getNoticeReadStatics(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId
    );

    @Operation(
        operationId = "NOTICE-005",
        summary = "공지사항 읽음 현황 상세 조회",
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
