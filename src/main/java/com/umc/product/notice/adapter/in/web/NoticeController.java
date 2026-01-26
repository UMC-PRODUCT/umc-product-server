package com.umc.product.notice.adapter.in.web;

import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.global.response.ApiResponse;
import com.umc.product.global.security.MemberPrincipal;
import com.umc.product.global.security.annotation.CurrentMember;
import com.umc.product.notice.adapter.in.web.dto.request.CreateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.request.SendNoticeReminderRequest;
import com.umc.product.notice.adapter.in.web.dto.request.UpdateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.response.CreateNoticeResponse;
import com.umc.product.notice.adapter.in.web.swagger.NoticeApi;
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

    /*
     * 공지사항 생성
     */
    @PostMapping
    public ApiResponse<CreateNoticeResponse> createNotice(@RequestBody @Valid CreateNoticeRequest request,
                                                          @CurrentMember MemberPrincipal memberPrincipal) {

        Long memberId = memberPrincipal.getMemberId();
        Long noticeId = manageNoticeUseCase.createNotice(request.toCommand(memberId));
        return ApiResponse.onSuccess(new CreateNoticeResponse(noticeId));
    }

    /*
     * 공지사항 삭제
     */
    @DeleteMapping("/{noticeId}")
    public void deleteNotice(@PathVariable Long noticeId, @CurrentMember MemberPrincipal memberPrincipal) {
        /*
         * TODO: challengerId 받아오는 방식 수정 필요
         */
        Long memberId = memberPrincipal.getMemberId();
        manageNoticeUseCase.deleteNotice(new DeleteNoticeCommand(memberId, noticeId));
    }

    /*
     * 공지사항 수정
     */
    @PatchMapping("/{noticeId}")
    public void updateNotice(@PathVariable Long noticeId, @RequestBody @Valid UpdateNoticeRequest request,
                             @CurrentMember MemberPrincipal memberPrincipal) {
        /*
         * TODO: challengerId 받아오는 방식 수정 필요
         */
        Long memberId = memberPrincipal.getMemberId();
        manageNoticeUseCase.updateNotice(request.toCommand(memberId, noticeId));
    }

    /*
     * 공지사항 리마인드 알림 보내기
     */
    @PostMapping("/{noticeId}/reminders")
    public void sendNoticeReminder(@PathVariable Long noticeId, @RequestBody @Valid SendNoticeReminderRequest request,
                                   @CurrentMember MemberPrincipal memberPrincipal) {
        /*
         * TODO: challengerId 받아오는 방식 수정 필요
         */
        Long memberId = memberPrincipal.getMemberId();
        manageNoticeUseCase.remindNotice(request.toCommand(memberId, noticeId));
    }

}
