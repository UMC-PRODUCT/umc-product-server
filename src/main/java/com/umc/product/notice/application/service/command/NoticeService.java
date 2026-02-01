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
    private static final String REMINDER_BODY_SUFFIX = " 공지를 확인해주세요.";
    // 도메인 내부 포트
    private final LoadNoticePort loadNoticePort;
    private final SaveNoticePort saveNoticePort;
    // 도메인 외부 UseCase
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final GetMemberRolesUseCase getMemberRolesUseCase;
    private final ManageNoticeContentUseCase manageNoticeContentUseCase;
    private final ManageFcmUseCase manageFcmUseCase;

    @Override
    public Long createNotice(CreateNoticeCommand command) {
        ChallengerInfo challenger = getChallengerByMemberAndGisu(
            command.memberId(),
            command.targetInfo().targetGisuId());

        // TODO: 작성 권한이 있는지 판단하는 로직 추가 필요

        Notice notice = Notice.create(
            command.title(),
            command.content(),
            challenger.challengerId(),
            command.shouldNotify()
        );

        Notice savedNotice = saveNoticePort.save(notice);

        // TODO: shouldNotify가 true일 경우, 발송 대상을 파악하고 알림 전송, 발송 후 notifiedAt 호출

        return savedNotice.getId();
    }

    @Override
    public void updateNoticeTitleOrContent(UpdateNoticeCommand command) {
        // TODO: 작성자가 일치하는지 여부를 검증

        // TODO: 작성자에 대한 상태 검증 추가

        Notice notice = findNoticeById(command.noticeId());

        // TODO: 작성자 일치 여부 검증 (도메인 로직으로 추가)

        notice.updateTitleOrContent(
            command.title(),
            command.content()
        );

        // 이미지, 투표, 링크 등은 별도의 command로 분리 구현
    }

    @Override
    public void deleteNotice(DeleteNoticeCommand command) {
        Long gisuId = getGisuUseCase.getActiveGisuId();

        ChallengerInfo challenger = getChallengerByMemberAndGisu(command.memberId(), gisuId);

        // TODO: 작성자에 대한 상태 검증 추가 (유효한 챌린저인지)

        Notice notice = findNoticeById(command.noticeId());

        // TODO: 공지 작성자인지 검증하는 로직 추가 (CheckAccess 어노테이션을 활용할 것)

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

    private boolean validateNoticeWritePermission(NoticeTargetInfo noticeTargetInfo, Long authorMemberId) {
        NoticeTargetPattern pattern = NoticeTargetPattern.from(noticeTargetInfo);
        return pattern.validatePermission(noticeTargetInfo, authorMemberId, getMemberRolesUseCase);
    }
}
