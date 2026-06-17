package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.statistics.ChapterProjectStatisticsInfo;
import com.umc.product.project.application.port.in.query.dto.statistics.ProjectStatisticsInfo;

/**
 * 프로젝트 지원/매칭 현황 통합 조회 UseCase.
 */
public interface GetProjectStatisticsUseCase {

    /**
     * 단건 프로젝트의 활성 멤버와 해당 멤버들이 작성한 지원 이력을 조회합니다.
     * <p>
     * 접근 권한: 해당 프로젝트의 PO/Sub-PM(본인 프로젝트) 또는 총괄단 → 멤버 단위 상세 포함,
     * 해당 지부장 / 해당 지부 소속 학교 회장·부회장 → 숫자(집계)만. 그 외는 {@code PROJECT_ACCESS_DENIED}.
     */
    ProjectStatisticsInfo getByProjectId(Long projectId, Long requesterMemberId);

    /**
     * 지부 내 전체 프로젝트의 활성 멤버와 BFF 요약 통계를 조회합니다.
     * <p>
     * 접근 권한: 총괄단 → 멤버 단위 상세 포함, 해당 지부장 / 해당 지부 소속 학교 회장·부회장 → 숫자(집계)만.
     * 그 외는 {@code PROJECT_ACCESS_DENIED}. (PO/Sub-PM 은 본인 프로젝트만 단건 조회로 본다.)
     */
    ChapterProjectStatisticsInfo getByChapterId(Long chapterId, Long requesterMemberId);
}
