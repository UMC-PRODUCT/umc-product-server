package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.ProjectMatchingOverviewInfo;
import com.umc.product.project.application.port.in.query.dto.ProjectMemberView;

public interface GetProjectMatchingOverviewUseCase {

    /**
     * 지부의 공개 프로젝트별 팀원 명단과 모집 정원(TO)을 조회합니다.
     * <p>
     * {@code view} 가 ADJUSTMENT 면 조정 권한을 검증하고, 최종 공개될 조정 명단을 반환합니다.
     */
    ProjectMatchingOverviewInfo getByChapterId(Long chapterId, ProjectMemberView view, Long requesterMemberId);
}
