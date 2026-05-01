package com.umc.product.survey.application.port.out;

import java.util.List;
import java.util.Map;

public interface LoadAnswerPort {

    /**
     * 특정 폼(투표)의 총 참여자 수를 반환합니다.
     */
    long countTotalParticipants(Long formId);

    /**
     * 특정 폼(투표)의 옵션별 득표수를 집계하여 반환합니다.
     * key: option ID, value: 투표 수
     */
    Map<Long, Long> countVotesByOptionId(Long formId);

    /**
     * 특정 사용자가 해당 폼(투표)에서 선택한 옵션 ID 목록을 반환합니다.
     */
    List<Long> findSelectedOptionIdsByMember(Long formId, Long memberId);

    /**
     * 특정 폼(투표)의 옵션별 투표자 ID 목록을 조회합니다.
     * key: option ID, value: 투표자 member ID 목록
     */
    Map<Long, List<Long>> findSelectedMemberIdsByOptionId(Long formId);
}
