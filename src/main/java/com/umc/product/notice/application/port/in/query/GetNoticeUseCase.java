package com.umc.product.notice.application.port.in.query;

import com.umc.product.notice.application.port.in.query.dto.GetNoticeStatusQuery;
import com.umc.product.notice.application.port.in.query.dto.NoticeInfo;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusResult;
import com.umc.product.notice.application.port.in.query.dto.NoticeReadStatusSummary;
import com.umc.product.notice.application.port.in.query.dto.NoticeSummary;
import com.umc.product.notice.dto.NoticeClassification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetNoticeUseCase {

    /*
     * 공지 전체조회 -> 간략한 정보 조회
     * @return 해당 scope에 맞는 공지사항 리스트 반환
     */
    Page<NoticeSummary> getAllNoticeSummaries(NoticeClassification info, Pageable pageable);


    /*
     * 검색어 기반 공지 전체 조회
     */
    Page<NoticeSummary> searchNoticesByKeyword(String keyword, NoticeClassification noticeClassification, Pageable pageable);

    /*
     * 공지 세부조회 -> 이때 조회수를 증가시켜야 함
     * @return 해당 공지의 세부 내용 조회
     */
    NoticeInfo getNoticeDetail(Long noticeId);

    /*
     * 공지 열람 현황 상세 조회
     * @return 해당 공지의 열람 현황
     */
    NoticeReadStatusResult getReadStatus(GetNoticeStatusQuery command);

    /*
     * 공지 열람 현황 단순 조회
     * @return 통계 현황
     */
    NoticeReadStatusSummary getReadStatistics(Long noticeId);
}
