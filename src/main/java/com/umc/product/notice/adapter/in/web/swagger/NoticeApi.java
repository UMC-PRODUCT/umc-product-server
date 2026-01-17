package com.umc.product.notice.adapter.in.web.swagger;

import com.umc.product.global.response.ApiResponse;
import com.umc.product.notice.adapter.in.web.dto.request.CreateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.request.SendNoticeReminderRequest;
import com.umc.product.notice.adapter.in.web.dto.request.UpdateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.response.CreateNoticeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface NoticeApi {

    @Operation(
            summary = "공지사항 생성 (임시저장)",
            description = "새로운 공지사항을 임시저장 상태로 생성합니다."
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
            @Parameter(description = "공지사항 생성 정보", required = true)
            @RequestBody @Valid CreateNoticeRequest request
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
            description = "공지사항 정보를 수정합니다."
    )
    void updateNotice(
            @Parameter(description = "공지사항 ID", required = true)
            @PathVariable Long noticeId,

            @Parameter(description = "수정할 공지사항 정보", required = true)
            @RequestBody @Valid UpdateNoticeRequest request
    );

    @Operation(
            summary = "공지사항 리마인더 발송",
            description = "공지사항을 읽지 않은 사용자에게 리마인더를 발송합니다."
    )
    void sendNoticeReminder(
            @Parameter(description = "공지사항 ID", required = true)
            @PathVariable Long noticeId,

            @Parameter(description = "리마인더 발송 정보", required = true)
            @RequestBody @Valid SendNoticeReminderRequest request
    );
}
