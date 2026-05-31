package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.statistics.ChapterProjectStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectStatisticsInfo;

/**
 * 프로젝트 지원/매칭 현황 통합 조회 UseCase.
 */
public interface GetProjectStatisticsUseCase {

    /**
     * 단건 프로젝트의 활성 멤버와 해당 멤버들이 작성한 지원 이력을 조회합니다.
     */
    ProjectStatisticsInfo getByProjectId(Long projectId);

    /**
     * 지부 내 전체 프로젝트의 활성 멤버와 BFF 요약 통계를 조회합니다.
     */
    ChapterProjectStatisticsInfo getByChapterId(Long chapterId);
}
