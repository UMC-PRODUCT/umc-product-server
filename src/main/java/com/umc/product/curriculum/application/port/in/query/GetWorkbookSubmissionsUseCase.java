package com.umc.product.curriculum.application.port.in.query;

import com.umc.product.curriculum.application.port.in.query.dto.GetWorkbookSubmissionsQuery;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
import java.util.List;

public interface GetWorkbookSubmissionsUseCase {

    /**
     * (운영진 기능) 학교, 주차, 스터디그룹별 제출된 ChallengerWorkbook 리스트 조회
     *
     * @param query schoolId, weekNo, studyGroupId, cursor, size
     * @return 워크북 제출 현황 리스트 (cursor 페이지네이션)
     */
    List<WorkbookSubmissionInfo> getSubmissions(GetWorkbookSubmissionsQuery query);
}
