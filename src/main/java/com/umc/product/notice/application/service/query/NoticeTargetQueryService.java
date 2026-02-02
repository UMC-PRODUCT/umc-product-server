package com.umc.product.notice.application.service.query;

import com.umc.product.notice.application.port.in.query.GetNoticeTargetUseCase;
import com.umc.product.notice.application.port.out.LoadNoticeTargetPort;
import com.umc.product.notice.domain.exception.NoticeDomainException;
import com.umc.product.notice.domain.exception.NoticeErrorCode;
import com.umc.product.notice.dto.NoticeTargetInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoticeTargetQueryService implements GetNoticeTargetUseCase {

    private final LoadNoticeTargetPort loadNoticeTargetPort;

    /**
     * 공지사항 ID로 해당 공지사항의 타겟을 조회합니다.
     */
    @Override
    public NoticeTargetInfo findByNoticeId(Long noticeId) {
        return loadNoticeTargetPort.findByNoticeId(noticeId)
            .map(NoticeTargetInfo::from)
            .orElseThrow(() -> new NoticeDomainException(NoticeErrorCode.NO_TARGET_FOUND,
                String.format("Notice ID %d에 대한 타겟 정보를 찾을 수 없습니다.", noticeId)));
    }
}
