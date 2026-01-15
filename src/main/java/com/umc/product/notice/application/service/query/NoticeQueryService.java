package com.umc.product.notice.application.service.query;

import com.umc.product.common.dto.ChallengerContext;
import com.umc.product.notice.application.port.in.query.GetNoticeUseCase;
import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusSummary;
import com.umc.product.notice.application.port.in.query.dto.NoticeSearchConditionInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeSummary;
import com.umc.product.notice.domain.enums.NoticeClassification;
import java.awt.print.Pageable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeQueryService implements GetNoticeUseCase {

    @Override
    public Page<NoticeSummary> getAllNoticeSummaries(ChallengerContext context, NoticeSearchConditionInfo info,
                                                     org.springframework.data.domain.Pageable pageable) {
        return null;
    }

    @Override
    public NoticeInfo getNoticeDetail(ChallengerContext context, Long noticeId) {
        return null;
    }

    @Override
    public List<NoticeReadStatusInfo> getReadStatus(GetNoticeStatusQuery command) {
        return null;
    }

    @Override
    public NoticeReadStatusSummary getReadStatistics(ChallengerContext context, Long noticeId) {
        return null;
    }
}
