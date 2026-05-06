package com.umc.product.notice.adapter.in.web.swagger;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.notice.adapter.in.web.dto.request.CreateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.request.SendNoticeReminderRequest;
import com.umc.product.notice.adapter.in.web.dto.request.UpdateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.response.command.CreateNoticeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Notice | 공지사항 Command", description = "공지사항 생성, 수정, 삭제")
public interface NoticeCommandControllerApi {

    @Operation(
        summary = "[NOTICE-201] 공지사항 생성",
        description = """
            - `mustRead=true`: 필독 공지로 지정 → 목록 최상단 고정 (UPMS에서 사용, 앱공지에서는 false로 설정)

            ---

            ## 운영진 공지 작성 방법

            `targetInfo.targetNoticeTab`을 `CHALLENGER` 이외의 값으로 설정하면 운영진 공지로 생성

            > **공통 제약**
            > - `targetGisuId` 는 항상 필수
            > - `targetChapterId` 지정 불가 (운영진 공지는 지부 단위 미지원)

            ### 중앙운영진 공지 (`targetSchoolId = null`)

            | 열람 대상 | targetNoticeTab | targetSchoolId | targetParts | 작성 권한 |
            |---|---|---|---|---|
            | 중앙운영진 전체 | `CENTRAL_MEMBER` | null | null | 총괄단 |
            | 중앙운영진 + 모든 학교회장단 | `SCHOOL_CORE` | null | null | 중앙운영진 |
            | 중앙운영진 + 모든 학교회장단 + 모든 파트장 | `SCHOOL_PART_LEADER` | null | null 또는 `[]` | 중앙운영진 |
            | 중앙운영진 + 모든 학교회장단 + 특정 파트 파트장 | `SCHOOL_PART_LEADER` | null | `["SPRINGBOOT"]` | 중앙운영진 |

            ### 교내운영진 공지 (`targetSchoolId` 지정 필수)

            | 열람 대상 | targetNoticeTab | targetSchoolId | targetParts | 작성 권한 |
            |---|---|---|---|---|
            | 해당 학교 파트장 전체 + 상위 운영진 | `SCHOOL_PART_LEADER` | schoolId | null 또는 `[]` | 해당 학교 회장단 |
            | 해당 학교 특정 파트 파트장 + 상위 운영진 | `SCHOOL_PART_LEADER` | schoolId | `["SPRINGBOOT"]` | 해당 학교 회장단 |
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "생성 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청"
        )
    })
    CreateNoticeResponse createNotice(
        @RequestBody @Valid CreateNoticeRequest request,

        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );


    @Operation(
        summary = "[NOTICE-202] 공지사항 삭제",
        description = "공지사항을 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "삭제 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "공지사항을 찾을 수 없음"
        )
    })
    void deleteNotice(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "[NOTICE-203] 공지사항 수정",
        description = "공지사항 내용을 수정합니다. mustRead=true로 설정하면 UPMS 필독 공지로 지정되어 목록 최상단에 고정되며, false로 변경하면 고정이 해제됩니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "수정 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "공지사항을 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청"
        )
    })
    void updateNotice(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(description = "수정할 공지사항 정보", required = true)
        @RequestBody @Valid UpdateNoticeRequest request,

        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "[NOTICE-204] 공지사항 리마인더 발송",
        description = "공지를 읽지 않은 사용자에게 푸시 알림을 재발송합니다. "
            + "읽음 현황 API(GET /notices/{noticeId}/status)에서 UNREAD 사용자 목록을 먼저 조회한 뒤, "
            + "리마인드할 챌린저 ID들을 targetIds로 전달하세요."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "발송 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "공지사항을 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청"
        )
    })
    void sendNoticeReminder(
        @Parameter(description = "공지사항 ID", required = true, example = "1")
        @PathVariable Long noticeId,

        @RequestBody @Valid SendNoticeReminderRequest request,

        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );

    @Operation(
        summary = "[NOTICE-205] 공지사항 읽음 처리",
        description = "공지사항을 읽음 처리합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "처리 성공"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "공지사항을 찾을 수 없음"
        )
    })
    void recordNoticeRead(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );
}
