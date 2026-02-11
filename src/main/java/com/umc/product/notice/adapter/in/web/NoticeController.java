package com.umc.product.notice.adapter.in.web;

import com.umc.product.authorization.adapter.in.aspect.CheckAccess;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.notice.adapter.in.web.dto.request.CreateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.request.SendNoticeReminderRequest;
import com.umc.product.notice.adapter.in.web.dto.request.UpdateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.response.command.CreateNoticeResponse;
import com.umc.product.notice.adapter.in.web.swagger.NoticeApi;
import com.umc.product.notice.application.port.in.command.ManageNoticeReadUseCase;
import com.umc.product.notice.application.port.in.command.ManageNoticeUseCase;
import com.umc.product.notice.application.port.in.command.dto.DeleteNoticeCommand;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Tag(name = Constants.NOTICE)
public class NoticeController implements NoticeApi {

    private final ManageNoticeUseCase manageNoticeUseCase;
    private final ManageNoticeReadUseCase manageNoticeReadUseCase;

    /*
     * 공지사항 생성
     */
    @PostMapping
    public ApiResponse<CreateNoticeResponse> createNotice(
        @RequestBody @Valid CreateNoticeRequest request,
        @CurrentMember MemberPrincipal memberPrincipal) {

        Long memberId = memberPrincipal.getMemberId();
        Long noticeId = manageNoticeUseCase.createNotice(request.toCommand(memberId));

        return ApiResponse.onSuccess(new CreateNoticeResponse(noticeId));
    }

    /*
     * 공지사항 삭제
     */
    @DeleteMapping("/{noticeId}")
    @CheckAccess(
        resourceType = ResourceType.NOTICE,
        resourceId = "#noticeId",
        permission = PermissionType.DELETE
    )
    public void deleteNotice(
        @PathVariable("noticeId") Long noticeId,
        @CurrentMember MemberPrincipal memberPrincipal) {

        Long memberId = memberPrincipal.getMemberId();
        manageNoticeUseCase.deleteNotice(new DeleteNoticeCommand(memberId, noticeId));
    }

    /*
     * 공지사항 수정
     */
    @PatchMapping("/{noticeId}")
    public void updateNotice(
        @PathVariable("noticeId") Long noticeId,
        @RequestBody @Valid UpdateNoticeRequest request,
        @CurrentMember MemberPrincipal memberPrincipal) {

        Long memberId = memberPrincipal.getMemberId();
        manageNoticeUseCase.updateNoticeTitleOrContent(
            request.toCommand(memberId, noticeId)
        );

    }

    /*
     * 공지사항 리마인드 알림 보내기
     */
    @PostMapping("/{noticeId}/reminders")
    public void sendNoticeReminder(
        @PathVariable("noticeId") Long noticeId,
        @RequestBody @Valid SendNoticeReminderRequest request,
        @CurrentMember MemberPrincipal memberPrincipal) {

        Long memberId = memberPrincipal.getMemberId();
        manageNoticeUseCase.remindNotice(request.toCommand(memberId, noticeId));
    }

    /*
     * 공지사항 읽음 처리
     */
    @PostMapping("/{noticeId}/read")
    @CheckAccess(
        resourceType = ResourceType.NOTICE,
        resourceId = "#noticeId",
        permission = PermissionType.READ
    )
    public ApiResponse<Void> recordNoticeRead(
        @PathVariable("noticeId") Long noticeId,
        @CurrentMember MemberPrincipal memberPrincipal
    ) {
        manageNoticeReadUseCase.recordRead(noticeId, memberPrincipal.getMemberId());
        return ApiResponse.onSuccess(null);
    }

}
