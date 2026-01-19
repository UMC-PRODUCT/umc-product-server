package com.umc.product.curriculum.application.port.out;

import com.umc.product.curriculum.application.port.in.query.dto.GetWorkbookSubmissionsQuery;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
import java.util.List;

public interface LoadWorkbookSubmissionPort {

    /**
     * 워크북 제출 현황 조회 (동적 필터링 + 커서 페이지네이션)
     *
     * @param query schoolId, weekNo, studyGroupId, cursor, size
     * @return 워크북 제출 현황 리스트 (size + 1개 조회하여 hasNext 판단)
     */
    List<WorkbookSubmissionInfo> findSubmissions(GetWorkbookSubmissionsQuery query);
}
