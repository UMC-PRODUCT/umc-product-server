package com.umc.product.notice.application.service.command;

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
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
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
        Long challengerId = command.authorChallengerId();

        /*
         * 기수 검증 (null이면 전체니까 필요 X)
         */
        if (command.targetInfo().targetGisuId() != null) {
            validateGisuExists(command.targetInfo().targetGisuId());
        }

        Notice notice = Notice.draft(
                command.title(), command.content(), challengerId, command.targetInfo().scope(),
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

        /*
         * 기수 검증 (null이면 전체니까 필요 X)
         */
        if (command.targetInfo().targetGisuId() != null) {
            validateGisuExists(command.targetInfo().targetGisuId());
        }

        notice.update(
                command.title(),
                command.content(),
                command.targetInfo().scope(),
                command.targetInfo().organizationId(),
                command.targetInfo().targetGisuId(),
                command.targetInfo().targetRoles(),
                command.targetInfo().targetParts(),
                command.shouldNotify(),
                notice.getStatus()
        );
        if (notice.isPublished()) {
            // TODO: 공지 수정 시 알림 전송 로직 추가 예정
        }
    }

    @Override
    public void deleteNotice(DeleteNoticeCommand command) {
        Notice notice = findNoticeById(command.noticeId());
        saveNoticePort.delete(notice);
    }

    @Override
    public void remindNotice(SendNoticeReminderCommand command) {
        Notice notice = findNoticeById(command.noticeId());
        // TODO: 알림 전송 로직 추가 예정
    }

    private Notice findNoticeById(Long noticeId) {
        return loadNoticePort.findNoticeById(noticeId)
                .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }

    private void validateGisuExists(Long gisuId) {
        Gisu gisu = gisuPort.findById(gisuId);
        if (gisu == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.GISU_NOT_FOUND);
        }
    }
}
