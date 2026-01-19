package com.umc.product.notice.application.service.command;

import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.notice.application.port.in.command.ManageNoticeUseCase;
import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.DeleteNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.PublishNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeReadPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.enums.NoticeClassification;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Gisu;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService implements ManageNoticeUseCase {

    private final LoadNoticePort loadNoticePort;
    private final SaveNoticePort saveNoticePort;
    private final LoadNoticeReadPort loadNoticeReadPort;
    private final SaveNoticeReadPort saveNoticeReadPort;
    private final LoadGisuPort gisuPort;

//    private final LoadChallengerPort loadChallengerPort;

    @Override
    public Long createDraftNotice(CreateNoticeCommand command) {
        /*
         * TODO: 챌린저 조회 관련 로직 추가 필요, 권한 검증 추가 예정
         */
        Challenger challenger
                = new Challenger(command.authorChallengerId(), ChallengerPart.DESIGN,
                command.targetInfo().targetGisuId());

        /*
         * 기수 검증 (null이면 전체니까 필요 X)
         */
        if (command.targetInfo().targetGisuId() != null) {
            validateGisuExists(command.targetInfo().targetGisuId());
        }


        Notice notice = Notice.draft(
                command.title(), command.content(), challenger.getId(), command.targetInfo().scope(),
                command.targetInfo().organizationId(), command.targetInfo().targetGisuId(),
                command.targetInfo().targetRoles(),
                command.targetInfo().targetParts(), command.shouldNotify()
        );

        Notice savedNotice = saveNoticePort.save(notice);
        return savedNotice.getId();
    }

    @Override
    public void publishNotice(PublishNoticeCommand command) {
        /*
         * TODO: 챌린저 조회 관련 로직 추가 필요, 권한 검증 추가 예정
         */

        Notice notice = findNoticeById(command.noticeId());
        notice.publish();
    }

    @Override
    public void updateNotice(UpdateNoticeCommand command) {
        Notice notice = findNoticeById(command.noticeId());

    }

    @Override
    public void deleteNotice(DeleteNoticeCommand command) {

    }

    @Override
    public void remindNotice(SendNoticeReminderCommand command) {

    }

    private Notice findNoticeById(Long noticeId) {
        return loadNoticePort.findNoticeById(noticeId)
                .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }

    private void validateGisuExists(Long gisuId) {
        Gisu gisu = gisuPort.findById(gisuId);
        if (gisu == null) {

    }
}
