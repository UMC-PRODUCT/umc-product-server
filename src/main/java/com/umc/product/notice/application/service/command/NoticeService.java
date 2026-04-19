package com.umc.product.notice.application.service.command;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import com.umc.product.notice.application.port.in.command.ManageNoticeUseCase;
import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.DeleteNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.ManageNoticeTargetPort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeReadPort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.notice.dto.NoticeTargetInfo;
import com.umc.product.notice.dto.NoticeTargetPattern;
import com.umc.product.notification.application.port.in.SendNotificationToAudienceUseCase;
import com.umc.product.notification.application.port.in.dto.AudienceNotificationCommand;
import java.time.Instant;
import java.util.List;
import java.util.Set;
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
    private final SaveNoticeTargetPort saveNoticeTargetPort;
    private final ManageNoticeTargetPort manageNoticeTargetPort;
    private final SaveNoticeReadPort saveNoticeReadPort;

    // 도메인 외부 UseCase
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final ManageNoticeContentUseCase manageNoticeContentUseCase;
    private final SendNotificationToAudienceUseCase sendNotificationToAudienceUseCase;

    @Override
    public Long createNotice(CreateNoticeCommand command) {
        if (!validateNoticeWritePermission(command.targetInfo(), command.memberId())) {
            throw new NoticeDomainException(NoticeErrorCode.NO_WRITE_PERMISSION);
        }

        Notice notice = Notice.create(
            command.title(),
            command.content(),
            command.memberId(),
            command.shouldNotify()
        );

        Notice savedNotice = saveNoticePort.save(notice);

        saveNoticeTargetPort.save(NoticeTarget.builder()
            .noticeId(savedNotice.getId())
            .targetGisuId(command.targetInfo().targetGisuId())
            .targetChapterId(command.targetInfo().targetChapterId())
            .targetSchoolId(command.targetInfo().targetSchoolId())
            .targetChallengerPart(command.targetInfo().targetParts())
            .build()
        );

        if (savedNotice.isNotificationRequired()) {
            String title = NOTICE_TITLE_PREFIX + savedNotice.getTitle();
            sendNotificationToAudienceUseCase.sendToAudience(
                new AudienceNotificationCommand(command.targetInfo(), title, NOTICE_BODY_SUFFIX)
            );
            savedNotice.markAsNotified(Instant.now());
        }

        return savedNotice.getId();
    }

    @Override
    public void updateNoticeTitleOrContent(UpdateNoticeCommand command) {
        Notice notice = findNoticeById(command.noticeId());

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

        // 관련 이미지, 투표, 링크 등도 모두 삭제
        manageNoticeContentUseCase.removeContentsByNoticeId(notice.getId(), command.memberId());

        // noticeRead, noticeTarget 삭제
        saveNoticeReadPort.deleteAllByNoticeId(notice.getId());
        manageNoticeTargetPort.deleteByNoticeId(notice.getId());

        // 공지 삭제
        saveNoticePort.delete(notice);
    }

    @Override
    public void remindNotice(SendNoticeReminderCommand command) {
        Notice notice = findNoticeById(command.noticeId());
        String title = NOTICE_REMINDER_TITLE_PREFIX + notice.getTitle();

        // challengerId → memberId 일괄 변환 (쿼리 1회)
        Set<Long> challengerIdSet = new java.util.HashSet<>(command.targetIds());
        List<Long> memberIds = getChallengerUseCase.getAllByIds(challengerIdSet).stream()
            .map(info -> info.memberId())
            .toList();

        // 대상 멤버 전체에 FCM 배치 발송 (토큰 조회 1회 + FCM 배치 전송)
        sendNotificationToAudienceUseCase.sendToMembers(memberIds, title, REMINDER_BODY_SUFFIX);
    }

    @Override
    public void incrementViewCount(Long noticeId) {
        saveNoticePort.incrementViewCount(noticeId);
    }

    // === PRIVATE METHODS ===

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
        return pattern.validatePermission(noticeTargetInfo, authorMemberId, getChallengerRoleUseCase);
    }


}
