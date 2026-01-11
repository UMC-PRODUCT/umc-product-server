package com.umc.product.notice.application.port.in.query;

import com.umc.product.challenger.domain.enums.OrganizationType;
import java.util.List;

public interface GetNoticeUseCase {

    /*
    * 공지 전체조회 -> 간략한 정보 조회
    * 검색 키워드는 선택적 요소
    * @return 해당 scope에 맞는 공지사항 리스트 반환
    * */
    List<NoticeSummary> getAllNoticeSummaries(Long challengerId, OrganizationType scope, String keyword);

    /*
    * 공지 세부조회 -> 이때 조회수를 증가시켜야 함
    * @return 해당 공지의 세부 내용 조회
    * */
    NoticeInfo getNoticeDetail(Long noticeId, Long challengerId);

    /*
    * 공지 열람 현황 상세 조회
    * @return 해당 공지의 열람 현황
    * */
    List<NoticeReadStatusInfo> getReadStatus(GetNoticeStatusCommand command);

    /*
    * 공지 열람 현황
    * @return 통계 현황
    * */
    NoticeReadStatusSummary getReadStatistics(Long noticeId, Long requesterId);
}
