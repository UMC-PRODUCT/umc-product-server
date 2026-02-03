package com.umc.product.notice.application.service.command;

import com.umc.product.authorization.application.port.in.query.GetMemberRolesUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import com.umc.product.notice.application.port.in.command.ManageNoticeUseCase;
import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.DeleteNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import com.umc.product.notice.application.port.in.query.GetNoticeTargetUseCase;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.notice.dto.NoticeTargetInfo;
import com.umc.product.notice.dto.NoticeTargetPattern;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.in.dto.NotificationCommand;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService implements ManageNoticeUseCase {

    private static final String NOTICE_REMINDER_TITLE_PREFIX = "[리마인드 공지] ";
    private static final String NOTICE_TITLE_PREFIX = "[새 공지] ";
    private static final String REMINDER_BODY_SUFFIX = " 공지를 확인해주세요.";
    private static final String NOTICE_BODY_SUFFIX = "새로운 공지가 등록되었습니다: ";
    // 도메인 내부 포트
    private final LoadNoticePort loadNoticePort;
    private final SaveNoticePort saveNoticePort;
    // 도메인 외부 UseCase
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetMemberRolesUseCase getMemberRolesUseCase;
    private final GetNoticeTargetUseCase getNoticeTargetUseCase;
    private final ManageNoticeContentUseCase manageNoticeContentUseCase;
    private final ManageFcmUseCase manageFcmUseCase;

    @Override
    public Long createNotice(CreateNoticeCommand command) {
        ChallengerInfo challenger = getChallengerByMemberAndGisu(
            command.memberId(),
            command.targetInfo().targetGisuId());

        if (!validateNoticeWritePermission(command.targetInfo(), command.memberId())) {
            throw new NoticeDomainException(NoticeErrorCode.NO_WRITE_PERMISSION);
        }

        Notice notice = Notice.create(
            command.title(),
            command.content(),
            challenger.challengerId(),
            command.shouldNotify()
        );

        Notice savedNotice = saveNoticePort.save(notice);

        if (savedNotice.isNotificationRequired()) {
            manageFcmUseCase.sendMessageByToken(new NotificationCommand(
                command.memberId(),
                NOTICE_TITLE_PREFIX + savedNotice.getTitle(),
                NOTICE_BODY_SUFFIX
            ));
            savedNotice.markAsNotified(Instant.now());
        }

        return savedNotice.getId();
    }

    @Override
    public void updateNoticeTitleOrContent(UpdateNoticeCommand command) {
        Notice notice = findNoticeById(command.noticeId());
        NoticeTargetInfo targets = getNoticeTargetUseCase.findByNoticeId(command.noticeId());

        /**
         * 작성자 일치 여부 검증 (이 메서드로 ACTIVE 상태인 챌린저만 올 수 있으므로 별도 상태 검증은 불필요)
         */
        boolean isAuthor = notice.isAuthorChallenger(
            getChallengerUseCase.getActiveByMemberIdAndGisuId(
                command.memberId(),
                targets.targetGisuId()
            ).challengerId()
        );

        if (!isAuthor) {
            throw new NoticeDomainException(NoticeErrorCode.NOTICE_AUTHOR_MISMATCH);
        }

        /**
         * 제목/내용만 수정
         */
        notice.updateTitleOrContent(
            command.title(),
            command.content()
        );

    }

    @Override
    public void deleteNotice(DeleteNoticeCommand command) {
        Notice notice = findNoticeById(command.noticeId());
        NoticeTargetInfo targets = getNoticeTargetUseCase.findByNoticeId(command.noticeId());
        /**
         * 작성자 일치 여부 검증 (이 메서드로 ACTIVE 상태인 챌린저만 올 수 있으므로 별도 상태 검증은 불필요)
         */
        boolean isAuthor = notice.isAuthorChallenger(
            getChallengerUseCase.getActiveByMemberIdAndGisuId(
                command.memberId(),
                targets.targetGisuId()
            ).challengerId()
        );

        if (!isAuthor) {
            throw new NoticeDomainException(NoticeErrorCode.NOTICE_AUTHOR_MISMATCH);
        }

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

    // === PRIVATE METHODS ===

    /**
     * 회원ID와 기수ID로 챌린저 조회
     */
    private ChallengerInfo getChallengerByMemberAndGisu(Long memberId, Long gisuId) {
        return getChallengerUseCase.getByMemberIdAndGisuId(memberId, gisuId);
    }

    /**
     * Notice ID로 Entity를 조회, 없으면 Exception 발생
     */
    private Notice findNoticeById(Long noticeId) {
        return loadNoticePort.findNoticeById(noticeId)
            .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }

    /**
     * 공지 작성 권한이 있는지 검증함
     */
    private boolean validateNoticeWritePermission(NoticeTargetInfo noticeTargetInfo, Long authorMemberId) {
        NoticeTargetPattern pattern = NoticeTargetPattern.from(noticeTargetInfo);
        return pattern.validatePermission(noticeTargetInfo, authorMemberId, getMemberRolesUseCase);
    }
}
