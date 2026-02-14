package com.umc.product.notice.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.notice.application.port.in.query.GetNoticeTargetUseCase;
import com.umc.product.notice.domain.Notice;
import com.umc.product.notice.dto.NoticeTargetInfo;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 공지사항 작성자 검증
 * <p>
 * 공지 대상의 기수 정보로 챌린저를 조회하여 작성자 본인인지 검증합니다.
 * targetGisuId가 null(전체 기수 대상)인 경우 현재 활성 기수를 사용합니다.
 */
@Component
@RequiredArgsConstructor
public class NoticeAuthorValidator {

    private final GetNoticeTargetUseCase getNoticeTargetUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;

    public void validate(Notice notice, Long memberId) {
        NoticeTargetInfo targets = getNoticeTargetUseCase.findByNoticeId(notice.getId());
        Long gisuId = targets.targetGisuId() != null
            ? targets.targetGisuId()
            : getGisuUseCase.getActiveGisuId();
        Long challengerId = getChallengerUseCase.getActiveByMemberIdAndGisuId(
            memberId, gisuId
        ).challengerId();

        notice.validateAuthorChallenger(challengerId);
    }
}
