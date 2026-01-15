package com.umc.product.notice.adapter.in.web;

import com.umc.product.common.dto.ChallengerContext;
import com.umc.product.global.constant.SwaggerTag;
import com.umc.product.global.constant.SwaggerTag.Constants;
import com.umc.product.notice.adapter.in.web.dto.request.CreateNoticeRequest;
import com.umc.product.notice.adapter.in.web.dto.request.SendNoticeReminderRequest;
import com.umc.product.notice.adapter.in.web.dto.request.UpdateNoticeRequest;
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
public class NoticeController {

    private final ManageNoticeUseCase manageNoticeUseCase;

    @PostMapping
    public void createNotice(@RequestBody @Valid CreateNoticeRequest request, ChallengerContext context) {
        manageNoticeUseCase.createDraftNotice(request.toCommand(), context);
    }

    @DeleteMapping("/{noticeId}")
    public void deleteNotice(@PathVariable Long noticeId, ChallengerContext context) {
        manageNoticeUseCase.deleteNotice(new DeleteNoticeCommand(noticeId), context);
    }

    @PatchMapping("/{noticeId}")
    public void updateNotice(@PathVariable Long noticeId, @RequestBody @Valid UpdateNoticeRequest request, ChallengerContext context) {
        manageNoticeUseCase.updateNotice(request.toCommand(noticeId), context);
    }

    @PostMapping("/{noticeId}/reminders")
    public void sendRemindNotice(@PathVariable Long noticeId, @RequestBody @Valid SendNoticeReminderRequest request, ChallengerContext context) {
        manageNoticeUseCase.remindNotice(request.toCommand(noticeId), context);
    }




}
