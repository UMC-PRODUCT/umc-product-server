package com.umc.product.notice.application.service.query;

import com.umc.product.notice.application.port.in.query.GetNoticeUseCase;
import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusResult;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusSummary;
import com.umc.product.notice.application.port.in.query.dto.NoticeSummary;
import com.umc.product.notice.domain.enums.NoticeClassification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NoticeQueryService implements GetNoticeUseCase {


    @Override
    public Page<NoticeSummary> getAllNoticeSummaries(NoticeClassification info, Pageable pageable) {
        return null;
    }

    @Override
    public Page<NoticeSummary> searchNoticesByKeyword(String keyword, Pageable pageable) {
        return null;
    }

    @Override
    public NoticeInfo getNoticeDetail(Long noticeId) {
        return null;
    }

    @Override
    public NoticeReadStatusResult getReadStatus(GetNoticeStatusQuery command) {
        return null;
    }


    @Override
    public NoticeReadStatusSummary getReadStatistics(Long noticeId) {
        return null;
    }
}
