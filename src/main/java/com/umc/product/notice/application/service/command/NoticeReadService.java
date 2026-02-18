package com.umc.product.notice.application.service.command;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfoWithStatus;
import com.umc.product.notice.application.port.in.command.ManageNoticeReadUseCase;
import com.umc.product.notice.application.port.in.query.GetNoticeTargetUseCase;
import com.umc.product.notice.application.port.out.LoadNoticePort;
import com.umc.product.notice.application.port.out.LoadNoticeReadPort;
import com.umc.product.notice.application.port.out.SaveNoticeReadPort;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.domain.NoticeRead;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.notice.dto.NoticeTargetInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    public void recordRead(Long noticeId, Long memberId) {
        Notice notice = findNoticeById(noticeId);
        NoticeTargetInfo target = getNoticeTargetUseCase.findByNoticeId(noticeId);

        Long challengerId;
        if (target.targetGisuId() != null) {
            challengerId = getChallengerUseCase.getActiveByMemberIdAndGisuId(memberId, target.targetGisuId())
                .challengerId();
        } else {
            ChallengerInfoWithStatus latest = getChallengerUseCase.getLatestActiveChallengerByMemberId(memberId);
            challengerId = latest.challengerId();
        }

        if (loadNoticeReadPort.existsRead(noticeId, challengerId)) {
            return;
        }

        NoticeRead noticeRead = NoticeRead.builder()
            .notice(notice)
            .challengerId(challengerId)
            .build();

        saveNoticeReadPort.saveRead(noticeRead);
    }

    private Notice findNoticeById(Long noticeId) {
        return loadNoticePort.findNoticeById(noticeId)
            .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NOTICE_NOT_FOUND));
    }
}
