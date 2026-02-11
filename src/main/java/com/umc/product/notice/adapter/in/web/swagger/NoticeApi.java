package com.umc.product.notice.adapter.in.web.swagger;

import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.notice.adapter.in.web.dto.request.CreateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.request.SendNoticeReminderRequest;
import com.umc.product.notice.adapter.in.web.dto.request.UpdateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.response.command.CreateNoticeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface NoticeApi {

    @Operation(
        summary = "공지사항 생성",
        description = "새로운 공지사항을 생성합니다. targetInfo로 대상 범위(기수/지부/학교/파트)를 지정하고, "
            + "shouldNotify=true로 설정하면 대상자에게 즉시 푸시 알림이 발송됩니다. "
            + "이미지/링크/투표는 공지 생성 후 별도 API로 추가해야 합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "생성 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청"
        )
    })
    ApiResponse<CreateNoticeResponse> createNotice(
        @RequestBody @Valid CreateNoticeRequest request,

        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );


    @Operation(
        summary = "공지사항 삭제",
        description = "공지사항을 삭제합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "삭제 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
        summary = "공지사항 수정",
        description = "공지사항 내용을 수정합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "수정 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "공지사항을 찾을 수 없음"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
        summary = "공지사항 리마인더 발송",
        description = "공지를 읽지 않은 사용자에게 푸시 알림을 재발송합니다. "
            + "읽음 현황 API(GET /notices/{noticeId}/status)에서 UNREAD 사용자 목록을 먼저 조회한 뒤, "
            + "리마인드할 챌린저 ID들을 targetIds로 전달하세요."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "발송 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "공지사항을 찾을 수 없음"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
        summary = "공지사항 읽음 처리",
        description = "공지사항을 읽음 처리합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "처리 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "공지사항을 찾을 수 없음"
        )
    })
    ApiResponse<Void> recordNoticeRead(
        @Parameter(description = "공지사항 ID", required = true)
        @PathVariable Long noticeId,

        @Parameter(hidden = true)
        @CurrentMember MemberPrincipal memberPrincipal
    );
}
