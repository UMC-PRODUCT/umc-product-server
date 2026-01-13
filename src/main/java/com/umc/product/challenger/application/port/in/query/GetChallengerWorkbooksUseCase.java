package com.umc.product.challenger.application.port.in.query;


import com.umc.product.challenger.application.port.in.query.dto.ChallengerWorkbookSummary;
import java.util.List;

public interface GetChallengerWorkbooksUseCase {

    /**
     * (운영진 기능) 주차, 스터디그룹별 제출된 ChallengerWorkbook 리스트 조회
     *
     * @param gisuId       기수 ID
     * @param weekNo       주차 (order_no)
     * @param studyGroupId 스터디그룹 ID (null이면 전체 그룹)
     */
    List<ChallengerWorkbookSummary> getByWeekAndStudyGroup(Long gisuId, Integer weekNo, Long studyGroupId);
}
