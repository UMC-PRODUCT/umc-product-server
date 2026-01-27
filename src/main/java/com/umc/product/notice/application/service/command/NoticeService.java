package com.umc.product.notice.application.service.command;

import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import com.umc.product.notice.application.port.in.command.ManageNoticeUseCase;
import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.DeleteNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.ReplaceNoticeImagesCommand;
import com.umc.product.notice.application.port.in.command.dto.ReplaceNoticeLinksCommand;
import com.umc.product.notice.application.port.in.command.dto.ReplaceNoticeVotesCommand;
import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.in.dto.NotificationCommand;
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

    private final LoadGisuPort loadGisuPort;
    private final LoadChallengerPort loadChallengerPort;

    private final ManageNoticeContentUseCase manageNoticeContentUseCase;

    private final ManageFcmUseCase manageFcmUseCase;

    private static final String NOTICE_REMINDER_TITLE_PREFIX = "[리마인드 공지] ";
    private static final String REMINDER_BODY_SUFFIX = " 공지를 확인해주세요.";

    @Override
    public Long createNotice(CreateNoticeCommand command) {
        Challenger challenger = findChallengerByMemberIdAndGisuId(command.memberId(), command.targetInfo().targetGisuId());
        challenger.validateChallengerStatus();
        /*
         * TODO: 권한 검증 추가 예정
         */

        /*
         * 기수 검증 (null이면 전체니까 필요 X)
         */
        if (command.targetInfo().targetGisuId() != null) {
            validateGisuExists(command.targetInfo().targetGisuId());
        }

        Notice notice = Notice.createNotice(
                command.title(), command.content(), challenger.getId(), command.targetInfo().scope(),
                command.targetInfo().organizationId(), command.targetInfo().targetGisuId(),
                command.targetInfo().targetRoles(),
                command.targetInfo().targetParts(), command.shouldNotify()
        );

        Notice savedNotice = saveNoticePort.save(notice);
        return savedNotice.getId();
    }

    @Override
    public void updateNotice(UpdateNoticeCommand command) {
        /*
         * 기수 검증 (null이면 전체니까 필요 X)
         */
        if (command.targetInfo().targetGisuId() != null) {
            validateGisuExists(command.targetInfo().targetGisuId());
        }

        Challenger challenger = findChallengerByMemberIdAndGisuId(command.memberId(), command.targetInfo().targetGisuId());
        challenger.validateChallengerStatus();

        Notice notice = findNoticeById(command.noticeId());

        /*
         * 작성자 검증
         */
        validateIsNoticeAuthor(challenger.getId(), notice.getAuthorChallengerId());

        /*
         * 내용 수정
         */
        notice.update(
                command.title(),
                command.content(),
                command.targetInfo().scope(),
                command.targetInfo().organizationId(),
                command.targetInfo().targetGisuId(),
                command.targetInfo().targetRoles(),
                command.targetInfo().targetParts(),
                command.shouldNotify()
        );

        /*
         * 이미지, 투표, 링크 등 수정
         */
        if (command.imageIds() != null) {
            manageNoticeContentUseCase.replaceImages(new ReplaceNoticeImagesCommand(command.imageIds()), notice.getId());
        }

        if (command.links() != null) {
            manageNoticeContentUseCase.replaceLinks(new ReplaceNoticeLinksCommand(command.links()), notice.getId());
        }

        if (command.voteIds() != null) {
            manageNoticeContentUseCase.replaceVotes(new ReplaceNoticeVotesCommand(command.voteIds()), notice.getId());
        }

    }

    @Override
    public void deleteNotice(DeleteNoticeCommand command) {
        Gisu nowGisu = loadGisuPort.findActiveGisu();

        Challenger challenger = findChallengerByMemberIdAndGisuId(command.memberId(), nowGisu.getId());
        challenger.validateChallengerStatus();

        Notice notice = findNoticeById(command.noticeId());

        /*
         * 작성자 검증
         */
        validateIsNoticeAuthor(challenger.getId(), notice.getAuthorChallengerId());

        /*
         * 관련 이미지, 투표, 링크 등도 모두 삭제
         */
        manageNoticeContentUseCase.removeContentsByNoticeId(notice.getId());

        saveNoticePort.delete(notice);
    }

    @Override
    public void remindNotice(SendNoticeReminderCommand command) {
        Notice notice = findNoticeById(command.noticeId());
        for (Long targetId : command.targetIds()) {
            manageFcmUseCase.sendMessageByToken(new NotificationCommand(targetId,
                    NOTICE_REMINDER_TITLE_PREFIX + notice.getTitle(),
                    REMINDER_BODY_SUFFIX))
            ;
        }
    }


    /*
     * private 메서드
     */
    private Challenger findChallengerByMemberIdAndGisuId(Long memberId, Long gisuId) {
        return loadChallengerPort.findByMemberIdAndGisuId(memberId, gisuId)
                .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_FOUND));
    }

    private Notice findNoticeById(Long noticeId) {
        return loadNoticePort.findNoticeById(noticeId)
                .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }

    private void validateGisuExists(Long gisuId) {
        Gisu gisu = loadGisuPort.findById(gisuId);
        if (gisu == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.GISU_NOT_FOUND);
        }
    }

    private void validateIsNoticeAuthor(Long challengerId, Long authorChallengerId) {
        if (!challengerId.equals(authorChallengerId)) {
            throw new NoticeDomainException(NoticeErrorCode.NOTICE_AUTHOR_MISMATCH);
        }
    }
}
