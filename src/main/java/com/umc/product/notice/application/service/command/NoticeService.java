package com.umc.product.notice.application.service.command;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
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
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
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

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;

    private final ManageNoticeContentUseCase manageNoticeContentUseCase;

    private final ManageFcmUseCase manageFcmUseCase;

    private static final String NOTICE_REMINDER_TITLE_PREFIX = "[리마인드 공지] ";
    private static final String REMINDER_BODY_SUFFIX = " 공지를 확인해주세요.";

    @Override
    public Long createNotice(CreateNoticeCommand command) {
        ChallengerInfo challenger = findChallengerByMemberIdAndGisuId(command.memberId(), command.targetInfo().targetGisuId());

        /*
         * TODO: 권한 검증 추가 예정
         */

        Notice notice = Notice.createNotice(
                command.title(), command.content(), challenger.challengerId(), command.targetInfo().scope(),
                command.targetInfo().organizationId(), command.targetInfo().targetGisuId(),
                command.targetInfo().targetRoles(),
                command.targetInfo().targetParts(), command.shouldNotify()
        );

        Notice savedNotice = saveNoticePort.save(notice);
        /*
         * TODO: 권한 추가 후 공지 알림 전송 로직 구현
         */
//        manageFcmUseCase.sendMessageByToken(new NotificationCommand());

        return savedNotice.getId();
    }

    @Override
    public void updateNotice(UpdateNoticeCommand command) {
        ChallengerInfo challenger = findChallengerByMemberIdAndGisuId(command.memberId(), command.targetInfo().targetGisuId());

        // TODO: 작성자에 대한 상태 검증 추가

        Notice notice = findNoticeById(command.noticeId());

        /*
         * 작성자 검증
         */
        validateIsNoticeAuthor(challenger.challengerId(), notice.getAuthorChallengerId());

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
        Long gisuId = getGisuUseCase.getActiveGisuId();

        ChallengerInfo challenger = findChallengerByMemberIdAndGisuId(command.memberId(), gisuId);

        // TODO: 작성자에 대한 상태 검증 추가
//        challenger.validateChallengerStatus();

        Notice notice = findNoticeById(command.noticeId());

        /*
         * 작성자 검증
         */
        validateIsNoticeAuthor(challenger.challengerId(), notice.getAuthorChallengerId());

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
    private ChallengerInfo findChallengerByMemberIdAndGisuId(Long memberId, Long gisuId) {
        return getChallengerUseCase.getByMemberIdAndGisuId(memberId, gisuId);
    }

    private Notice findNoticeById(Long noticeId) {
        return loadNoticePort.findNoticeById(noticeId)
                .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }

    private void validateIsNoticeAuthor(Long challengerId, Long authorChallengerId) {
        if (!challengerId.equals(authorChallengerId)) {
            throw new NoticeDomainException(NoticeErrorCode.NOTICE_AUTHOR_MISMATCH);
        }
    }
}
