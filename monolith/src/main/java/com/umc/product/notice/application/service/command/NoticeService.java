package com.umc.product.notice.application.service.command;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.notice.application.port.in.command.ManageNoticeContentUseCase;
import com.umc.product.notice.application.port.in.command.ManageNoticeUseCase;
import com.umc.product.notice.application.port.in.command.dto.CreateNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.DeleteNoticeCommand;
import com.umc.product.notice.application.port.in.command.dto.SendNoticeReminderCommand;
import com.umc.product.notice.application.port.in.command.dto.UpdateNoticeCommand;
import com.umc.product.notice.application.port.in.query.GetNoticeTargetUseCase;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.ManageNoticeTargetPort;
import com.umc.product.notice.application.port.out.SaveNoticePort;
import com.umc.product.notice.application.port.out.SaveNoticeReadPort;
import com.umc.product.notice.application.port.out.SaveNoticeTargetPort;
import com.umc.product.notice.application.service.query.NoticeAudienceResolver;
import com.umc.product.notice.application.service.query.NoticeReadAudienceService;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeTarget;
import com.umc.product.notice.domain.NoticeTargetInfo;
import com.umc.product.notice.domain.enums.NoticeTargetPattern;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.notification.application.port.in.RequestFcmNotificationUseCase;
import com.umc.product.notification.application.port.in.dto.RequestFcmNotificationCommand;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeService implements ManageNoticeUseCase {

    private static final ZoneId REMINDER_ZONE = ZoneId.of("Asia/Seoul");

    // TODO: 기획단과 PREFIX 협의 후에 수정해야 합니다.
    private static final String NOTICE_REMINDER_TITLE_PREFIX = "[⏰ 공지사항 리마인드] ";
    private static final String NOTICE_TITLE_PREFIX = "[\uD83D\uDCE2 새로운 공지사항] "; // loudspeaker emoji

    // TODO: 일단 SUFFIX는 없는걸로 ..
    private static final String REMINDER_BODY_SUFFIX = "";
    private static final String NOTICE_BODY_SUFFIX = "";

    // 도메인 내부 포트
    private final LoadNoticePort loadNoticePort;
    private final SaveNoticePort saveNoticePort;
    private final SaveNoticeTargetPort saveNoticeTargetPort;
    private final ManageNoticeTargetPort manageNoticeTargetPort;
    private final SaveNoticeReadPort saveNoticeReadPort;

    // 도메인 외부 UseCase
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;
    private final CheckPermissionUseCase checkPermissionUseCase;
    private final GetNoticeTargetUseCase getNoticeTargetUseCase;
    private final ManageNoticeContentUseCase manageNoticeContentUseCase;
    private final RequestFcmNotificationUseCase requestFcmNotificationUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final NoticeAudienceResolver noticeAudienceResolver;
    private final NoticeReadAudienceService noticeReadAudienceService;
    private final Clock clock;

    @Override
    public List<Long> createNoticeBulk(List<CreateNoticeCommand> commands) {
        if (commands.isEmpty()) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>(commands.size());
        for (CreateNoticeCommand command : commands) {
            ids.add(createNotice(command));
        }
        return ids;
    }

    @Audited(
        domain = Domain.NOTICE,
        action = AuditAction.CREATE,
        targetType = "Notice",
        targetId = "#result",
        description = "'공지사항을 생성했습니다.'"
    )
    @Override
    public Long createNotice(CreateNoticeCommand command) {
        validateSelectableTargetParts(command.targetInfo());

        if (!validateNoticeWritePermission(command.targetInfo(), command.memberId())) {
            throw new NoticeDomainException(NoticeErrorCode.NO_WRITE_PERMISSION);
        }

        Notice notice = Notice.create(
            command.title(),
            command.content(),
            command.memberId(),
            command.shouldNotify(),
            command.mustRead(),
            command.upmsNoticeType()
        );

        Notice savedNotice = saveNoticePort.save(notice);

        saveNoticeTargetPort.save(NoticeTarget.builder()
            .noticeId(savedNotice.getId())
            .targetGisuId(command.targetInfo().targetGisuId())
            .targetChapterId(command.targetInfo().targetChapterId())
            .targetSchoolId(command.targetInfo().targetSchoolId())
            .targetChallengerPart(command.targetInfo().targetParts())
            .targetNoticeTab(command.targetInfo().targetNoticeTab())
            .build()
        );

        // 알람을 전송하도록 설정된 공지사항인 경우 알람 전송 시도
        if (savedNotice.isNotificationRequired()) {
            String alarmTitle = StringUtils.abbreviate(NOTICE_TITLE_PREFIX + savedNotice.getTitle(), 25);
            String alarmBody = StringUtils.abbreviate(NOTICE_BODY_SUFFIX + savedNotice.getContent(), 40);

            requestFcmNotificationUseCase.request(
                RequestFcmNotificationCommand.builder()
                    .requesterMemberId(command.memberId())
                    .memberIds(noticeAudienceResolver.resolve(command.targetInfo()))
                    .title(alarmTitle)
                    .body(alarmBody)
                    .deepLink("umc://notice/" + savedNotice.getId())
                    .build()
            );
            savedNotice.markAsNotified(Instant.now()); // 알람 발송 완료 처리
        }

        return savedNotice.getId();
    }

    @Audited(
        domain = Domain.NOTICE,
        action = AuditAction.UPDATE,
        targetType = "Notice",
        targetId = "#command.noticeId()",
        description = "'공지사항을 수정했습니다.'"
    )
    @Override
    public void updateNoticeTitleOrContent(UpdateNoticeCommand command) {
        Notice notice = findNoticeById(command.noticeId());

        notice.updateTitleOrContent(command.title(), command.content());
        notice.updateMustRead(command.mustRead());

    }

    @Audited(
        domain = Domain.NOTICE,
        action = AuditAction.DELETE,
        targetType = "Notice",
        targetId = "#command.noticeId()",
        description = "'공지사항을 삭제했습니다.'"
    )
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

    @Audited(
        domain = Domain.NOTICE,
        action = AuditAction.REMIND,
        targetType = "Notice",
        targetId = "#command.noticeId()",
        description = "'공지사항 재알림 발송을 요청했습니다.'"
    )
    @Override
    public void remindNotice(SendNoticeReminderCommand command) {
        sendReminder(command.noticeId(), command.memberId(), Optional.of(new HashSet<>(command.targetIds())));
    }

    @Audited(
        domain = Domain.NOTICE,
        action = AuditAction.REMIND,
        targetType = "Notice",
        targetId = "#noticeId",
        description = "'공지사항 전체 재알림 발송을 요청했습니다.'"
    )
    @Override
    public void remindAllUnread(Long noticeId, Long memberId) {
        sendReminder(noticeId, memberId, Optional.empty());
    }

    private void sendReminder(Long noticeId, Long memberId, Optional<Set<Long>> selectedChallengerIds) {
        Notice notice = findNoticeById(noticeId);
        checkPermissionUseCase.checkOrThrow(memberId,
            ResourcePermission.of(ResourceType.NOTICE, notice.getId(), PermissionType.EDIT));
        NoticeTargetInfo target = getNoticeTargetUseCase.findByNoticeId(notice.getId());
        if (target.isStaffNotice()) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_NOTICE_STATUS_FOR_REMINDER);
        }

        // 관리 권한으로 열람할 수 있어도 공지의 게시 대상에 해당할 때만 재알림을 받는다.
        Set<Long> recipientIds = new HashSet<>(noticeAudienceResolver.resolve(target));
        List<Long> unreadMemberIds = selectedChallengerIds
            .map(ids -> noticeReadAudienceService.listUnreadRecipientMemberIds(noticeId, ids))
            .orElseGet(() -> noticeReadAudienceService.listAllUnreadRecipientMemberIds(noticeId));
        List<Long> memberIds = unreadMemberIds.stream()
            .filter(recipientIds::contains)
            .toList();
        if (memberIds.isEmpty()) {
            throw new NoticeDomainException(NoticeErrorCode.NO_UNREAD_REMINDER_TARGET);
        }

        if (selectedChallengerIds.isEmpty()) {
            LocalDate today = LocalDate.ofInstant(clock.instant(), REMINDER_ZONE);
            if (!saveNoticePort.tryReserveReminder(notice.getId(), today)) {
                throw new NoticeDomainException(NoticeErrorCode.REMINDER_DAILY_LIMIT_REACHED);
            }
        }

        String alarmTitle = NOTICE_REMINDER_TITLE_PREFIX + notice.getTitle();
        String alarmBody = StringUtils.abbreviate(REMINDER_BODY_SUFFIX + notice.getContent(), 40);
        requestFcmNotificationUseCase.request(
            RequestFcmNotificationCommand.builder()
                .requesterMemberId(memberId)
                .memberIds(memberIds)
                .title(alarmTitle)
                .body(alarmBody)
                .deepLink("umc://notice/" + notice.getId())
                .build()
        );
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
     * 공지 대상 파트가 현재 사용 가능한(선택 가능한) 파트인지 검증한다. 레거시 파트를 대상으로 지정하면 신규 공지를 만들 수 없다. 대상 파트 미지정(전체 대상)은 허용한다.
     */
    private void validateSelectableTargetParts(NoticeTargetInfo noticeTargetInfo) {
        List<ChallengerPart> targetParts = noticeTargetInfo.targetParts();
        if (targetParts == null || targetParts.isEmpty()) {
            return;
        }
        boolean hasLegacyPart = targetParts.stream().anyMatch(part -> !part.isSelectable());
        if (hasLegacyPart) {
            throw new NoticeDomainException(NoticeErrorCode.INVALID_TARGET_PART);
        }
    }

    /**
     * 공지 작성 권한이 있는지 검증함
     */
    private boolean validateNoticeWritePermission(NoticeTargetInfo noticeTargetInfo, Long authorMemberId) {
        NoticeTargetPattern pattern = NoticeTargetPattern.from(noticeTargetInfo);
        pattern.validateTargetSetting(noticeTargetInfo);

        // 시스템 관리자 권한은 활성 기수 유무와 무관하다. 대상 조합 검증은 동일하게 적용한다.
        if (getChallengerRoleUseCase.isSuperAdmin(authorMemberId)) {
            return true;
        }

        return pattern.validatePermissionInGisu(noticeTargetInfo, authorMemberId,
            getChallengerRoleUseCase, getGisuUseCase.getActiveGisuId());
    }
}
