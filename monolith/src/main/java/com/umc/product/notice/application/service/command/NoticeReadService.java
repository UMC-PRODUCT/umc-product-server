package com.umc.product.notice.application.service.command;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.authorization.application.port.in.CheckPermissionUseCase;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerBasicInfo;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerStatus;
import com.umc.product.notice.application.port.in.command.ManageNoticeReadUseCase;
import com.umc.product.notice.application.port.in.query.GetNoticeTargetUseCase;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
import com.umc.product.notice.application.port.out.SaveNoticeReadPort;
import com.umc.product.notice.domain.NoticeTargetInfo;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NoticeReadService implements ManageNoticeReadUseCase {

    private final LoadNoticePort loadNoticePort;
    private final LoadNoticeReadPort loadNoticeReadPort;
    private final SaveNoticeReadPort saveNoticeReadPort;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetNoticeTargetUseCase getNoticeTargetUseCase;
    private final CheckPermissionUseCase checkPermissionUseCase;

    @Override
    public void recordRead(Long noticeId, Long memberId) {
        loadNoticePort.findNoticeById(noticeId)
            .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
        NoticeTargetInfo target = getNoticeTargetUseCase.findByNoticeId(noticeId);

        List<ChallengerBasicInfo> history = getChallengerUseCase.getAllBasicByMemberIds(Set.of(memberId))
            .getOrDefault(memberId, List.of());
        Long challengerId = readerChallengerId(noticeId, memberId, target.targetGisuId(), history);
        List<Long> challengerIds = history.stream().map(ChallengerBasicInfo::challengerId).toList();
        if (loadNoticeReadPort.countReadsByChallengerIdIn(noticeId, challengerIds) > 0) {
            return;
        }

        saveNoticeReadPort.recordReadIfAbsent(noticeId, challengerId);
    }

    private Long readerChallengerId(Long noticeId, Long memberId, Long targetGisuId,
                                    List<ChallengerBasicInfo> history) {
        if (targetGisuId != null) {
            ChallengerBasicInfo target = history.stream().filter(row -> targetGisuId.equals(row.gisuId()))
                .findFirst().orElse(null);
            if (target != null && target.challengerStatus() == ChallengerStatus.ACTIVE) {
                return target.challengerId();
            }
            if (target != null && target.challengerStatus() != ChallengerStatus.GRADUATED) {
                throw new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_ACTIVE);
            }

            // 수료생과 다른 기수 운영진은 해당 공지를 열람할 수 있을 때만 본인 이력으로 읽음을 남긴다.
            checkPermissionUseCase.checkOrThrow(memberId,
                ResourcePermission.of(ResourceType.NOTICE, noticeId, PermissionType.READ));
            if (target != null) {
                return target.challengerId();
            }
        }
        return getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId).challengerId();
    }
}
