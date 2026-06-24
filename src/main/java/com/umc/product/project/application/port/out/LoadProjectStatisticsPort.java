package com.umc.product.project.application.port.out;

import java.util.Collection;
import java.util.List;

import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsApprovedApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMatchingRoundRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsProjectRow;

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
     * 로그인 사용자 공개 매칭 통계에 포함할 공개 프로젝트를 조회합니다.
     */
    List<ProjectStatisticsProjectRow> listPublicProjectsByChapterId(Long chapterId);

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
     * 지정한 프로젝트들의 ACTIVE 프로젝트 멤버를 조회합니다.
     */
    List<ProjectStatisticsMemberRow> listActiveMembersByProjectIds(Collection<Long> projectIds);

    /**
     * 공개 프로젝트에 속한 ACTIVE 프로젝트 멤버 중 실제 매칭 집계 대상 멤버를 조회합니다.
     */
    List<ProjectStatisticsMemberRow> listPublicActiveMembersByChapterId(Long chapterId);

    /**
     * 프로젝트에 제출된 이후 지원 이력을 조회합니다. DRAFT/CANCELLED 는 지원 현황에서 제외합니다.
     */
    List<ProjectStatisticsApplicationRow> listCountedApplicationsByProjectIds(Collection<Long> projectIds);

    /**
     * 공개 매칭 통계의 차수 귀속에 사용할 합격 지원서를 조회합니다.
     */
    List<ProjectStatisticsApprovedApplicationRow> listApprovedApplicationsByProjectIds(Collection<Long> projectIds);
}
