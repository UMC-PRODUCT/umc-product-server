package com.umc.product.notice.adapter.in.web.swagger;

import com.umc.product.global.response.ApiResponse;
import com.umc.product.notice.adapter.in.web.dto.request.CreateDraftNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.request.SendNoticeReminderRequest;
import com.umc.product.notice.adapter.in.web.dto.request.UpdateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.response.CreateDraftNoticeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface NoticeApi {

    @Operation(
            summary = "공지사항 생성 (임시저장)",
            description = "새로운 공지사항을 임시저장(DRAFT) 상태로 생성합니다."
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
    ApiResponse<CreateDraftNoticeResponse> createDraftNotice(
            @Parameter(description = "공지사항 생성 정보", required = true)
            @RequestBody @Valid CreateDraftNoticeRequest request
    );

    @Operation(
            summary = "공지사항 최종 게시",
            description = "임시저장 상태의 공지사항을 최종 게시(PUBLISHED)합니다. DRAFT → PUBLISHED 상태로 변경됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "게시 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "공지사항을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "이미 게시된 공지사항이거나 게시 불가능한 상태"
            )
    })
    void publishNotice(
            @Parameter(description = "공지사항 ID", required = true)
            @PathVariable Long noticeId
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
            @PathVariable Long noticeId
    );

    @Operation(
            summary = "공지사항 수정",
            description = "공지사항 내용을 수정합니다. DRAFT/PUBLISHED 상태 모두 수정 가능합니다. "
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
            @RequestBody @Valid UpdateNoticeRequest request
    );

    @Operation(
            summary = "공지사항 리마인더 발송",
            description = "공지사항을 읽지 않은 사용자에게 리마인더 알림을 발송합니다."
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
            @Parameter(description = "공지사항 ID", required = true)
            @PathVariable Long noticeId,

            @Parameter(description = "리마인더 발송 정보", required = true)
            @RequestBody @Valid SendNoticeReminderRequest request
    );
}
