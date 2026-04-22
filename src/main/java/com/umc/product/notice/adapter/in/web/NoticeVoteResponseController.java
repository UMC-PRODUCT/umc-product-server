package com.umc.product.notice.adapter.in.web;

import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.notice.adapter.in.web.dto.request.SubmitNoticeVoteResponseRequest;
import com.umc.product.notice.adapter.in.web.dto.request.UpdateNoticeVoteResponseRequest;
import com.umc.product.notice.application.port.in.command.ManageNoticeVoteResponseUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Tag(name = "Notice Vote Response | 공지사항 투표 응답", description = "공지사항 투표에 응답 제출/수정/취소")
public class NoticeVoteResponseController {

    private final ManageNoticeVoteResponseUseCase manageNoticeVoteResponseUseCase;

    @PostMapping("/{noticeId}/votes/responses")
    @Operation(
        summary = "공지사항 투표 응답 제출",
        description = """
            공지사항 투표에 응답을 제출합니다.
            - 투표 기간(OPEN) 내에서만 가능합니다. 시작 전이면 `VOTE_NOT_STARTED`, 종료 후면 `VOTE_CLOSED`.
            - 이미 제출한 응답이 있으면 `FORM_RESPONSE_ALREADY_EXISTS`. 수정 또는 취소는 PUT을 사용하세요.
            - 단일 선택(RADIO) 투표는 `optionIds`에 1개, 복수 선택(CHECKBOX)은 여러 개.
            """
    )
    public void submitVoteResponse(
        @PathVariable Long noticeId,
        @RequestBody @Valid SubmitNoticeVoteResponseRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        manageNoticeVoteResponseUseCase.submit(
            request.toCommand(noticeId, memberPrincipal.getMemberId())
        );
    }

    @PutMapping("/{noticeId}/votes/responses")
    @Operation(
        summary = "공지사항 투표 응답 수정/취소",
        description = """
            기존에 제출한 공지사항 투표 응답의 선택지를 교체하거나 취소합니다.
            - 투표 기간(OPEN) 내에서만 가능합니다.
            - `optionIds`를 비어있는 배열 `[]` 로 보내면 기존 응답이 취소됩니다 (응답 삭제).
            - 비어있지 않으면 기존 응답이 새 선택지로 교체됩니다.
            - 기존 응답이 없으면 `FORM_RESPONSE_NOT_FOUND`.
            """
    )
    public void updateOrCancelVoteResponse(
        @PathVariable Long noticeId,
        @RequestBody @Valid UpdateNoticeVoteResponseRequest request,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        manageNoticeVoteResponseUseCase.updateOrCancel(
            request.toCommand(noticeId, memberPrincipal.getMemberId())
        );
    }
}
