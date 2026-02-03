package com.umc.product.notice.application.service.command;

import com.umc.product.authorization.application.port.in.query.GetMemberRolesUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import com.umc.product.notice.application.port.in.command.ManageNoticeUseCase;
import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.DeleteNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import com.umc.product.notice.application.port.in.query.GetNoticeTargetUseCase;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.notice.dto.NoticeTargetInfo;
import com.umc.product.notice.dto.NoticeTargetPattern;
import com.umc.product.notification.application.port.in.ManageFcmUseCase;
import com.umc.product.notification.application.port.in.dto.NotificationCommand;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;
import com.umc.product.member.application.port.in.query.MemberInfo;
import com.umc.product.organization.application.port.in.query.GetChapterUseCase;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
    private final LoadChallengerPort loadChallengerPort;

    // 도메인 외부 UseCase
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetMemberRolesUseCase getMemberRolesUseCase;
    private final GetNoticeTargetUseCase getNoticeTargetUseCase;
    private final ManageFcmUseCase manageFcmUseCase;
    private final ManageNoticeContentUseCase manageNoticeContentUseCase;
    private final GetMemberUseCase getMemberUseCase;
    private final GetChapterUseCase getChapterUseCase;

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

        saveNoticeTargetPort.save(NoticeTarget.builder()
            .noticeId(savedNotice.getId())
            .targetGisuId(command.targetInfo().targetGisuId())
            .targetChapterId(command.targetInfo().targetChapterId())
            .targetSchoolId(command.targetInfo().targetSchoolId())
            .targetChallengerPart(command.targetInfo().targetParts())
            .build()
        );

        /**
         * 공지 알림 전송
         */
        if (savedNotice.isNotificationRequired()) {
            List<Long> targetIds = resolveTargetChallengerIds(command.targetInfo());
            for (Long targetId : targetIds) {
                manageFcmUseCase.sendMessageByToken(new NotificationCommand(
                    targetId,
                    NOTICE_TITLE_PREFIX + savedNotice.getTitle(),
                    NOTICE_BODY_SUFFIX
                ));
            }
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

    /**
     * 공지 작성 권한이 있는지 검증함
     */
    private boolean validateNoticeWritePermission(NoticeTargetInfo noticeTargetInfo, Long authorMemberId) {
        NoticeTargetPattern pattern = NoticeTargetPattern.from(noticeTargetInfo);
        return pattern.validatePermission(noticeTargetInfo, authorMemberId, getMemberRolesUseCase);
    }

    /**
     * NoticeTargetInfo에 매칭되는 챌린저 ID 목록을 조회합니다. (알림 전송용)
     *
     * TODO: 헥사고날 관점 상 추후 리팩토링 필요
     * @return 대상 챌린저 ID 리스트
     */
    private List<Long> resolveTargetChallengerIds(NoticeTargetInfo targetInfo) {
        if (targetInfo == null || targetInfo.targetGisuId() == null) {
            return List.of();
        }

        List<Challenger> challengers = loadChallengerPort.findByGisuId(targetInfo.targetGisuId());
        List<Long> targetIds = new ArrayList<>();

        for (Challenger challenger : challengers) {
            MemberInfo memberInfo = getMemberUseCase.getById(challenger.getMemberId());
            Long schoolId = memberInfo.schoolId();
            Long chapterId = getChapterUseCase.byGisuAndSchool(challenger.getGisuId(), schoolId).id();

            if (targetInfo.isTarget(
                challenger.getGisuId(),
                chapterId,
                schoolId,
                challenger.getPart()
            )) {
                targetIds.add(challenger.getId());
            }
        }

        return targetIds;
    }
}
