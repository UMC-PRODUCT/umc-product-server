package com.umc.product.project.application.port.out;

import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMatchingRoundRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsProjectRow;
import java.util.Collection;
import java.util.List;

public interface LoadProjectStatisticsPort {

    /**
     * 단건 프로젝트의 통계 집계에 필요한 프로젝트 기준 정보를 조회합니다.
     */
    ProjectStatisticsProjectRow getProjectById(Long projectId);

    /**
     * 지부 내 전체 프로젝트의 통계 집계 기준 정보를 조회합니다.
     */
    List<ProjectStatisticsProjectRow> listProjectsByChapterId(Long chapterId);

    /**
     * 지부 내 매칭 차수를 집계 표시 순서대로 조회합니다.
     */
    List<ProjectStatisticsMatchingRoundRow> listMatchingRoundsByChapterId(Long chapterId);

    /**
     * 단건 프로젝트의 ACTIVE 프로젝트 멤버를 조회합니다.
     */
    List<ProjectStatisticsMemberRow> listActiveMembersByProjectId(Long projectId);

    /**
     * 지부 내 전체 프로젝트의 ACTIVE 프로젝트 멤버를 조회합니다.
     */
    List<ProjectStatisticsMemberRow> listActiveMembersByChapterId(Long chapterId);

    /**
     * 프로젝트에 제출된 이후 지원 이력을 조회합니다. DRAFT/CANCELLED 는 지원 현황에서 제외합니다.
     */
    List<ProjectStatisticsApplicationRow> listCountedApplicationsByProjectIds(Collection<Long> projectIds);
}
