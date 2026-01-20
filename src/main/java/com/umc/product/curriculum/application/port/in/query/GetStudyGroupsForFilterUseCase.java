package com.umc.product.curriculum.application.port.in.query;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.dto.StudyGroupFilterInfo;
import java.util.List;

public interface GetStudyGroupsForFilterUseCase {

    /**
     * 워크북 제출 현황 필터용 스터디 그룹 목록 조회
     *
     * @param schoolId 학교 ID
     * @param part 파트
     * @return 스터디 그룹 목록 (id, name)
     */
    List<StudyGroupFilterInfo> getStudyGroupsForFilter(Long schoolId, ChallengerPart part);
}
