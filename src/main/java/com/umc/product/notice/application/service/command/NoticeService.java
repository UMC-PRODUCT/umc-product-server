package com.umc.product.notice.application.service.command;

import com.umc.product.common.dto.ChallengerContext;
import com.umc.product.notice.application.port.in.command.ManageNoticeUseCase;
import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.DeleteNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.PublishNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService implements ManageNoticeUseCase {
    @Override
    public Long createDraftNotice(CreateNoticeCommand command, ChallengerContext context) {
        return null;
    }

    @Override
    public void publishNotice(PublishNoticeCommand command, ChallengerContext context) {

    }

    @Override
    public void updateNotice(UpdateNoticeCommand command, ChallengerContext context) {

    }

    @Override
    public void deleteNotice(DeleteNoticeCommand command, ChallengerContext context) {

    }

    @Override
    public void remindNotice(SendNoticeReminderCommand command, ChallengerContext context) {

    }
}
