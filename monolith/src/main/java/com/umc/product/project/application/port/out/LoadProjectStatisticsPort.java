package com.umc.product.project.application.port.out;

import java.util.Collection;
import java.util.List;

import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMatchedMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsProjectRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsTargetProjectRow;

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
     * 지정 프로젝트 중 임시저장·승인대기를 제외한 지원 통계 대상을 조회합니다.
     */
    List<ProjectStatisticsTargetProjectRow> listApplicationStatisticsProjectsByIds(Collection<Long> projectIds);

    /**
     * 로그인 사용자 공개 매칭 통계에 포함할 공개 프로젝트를 조회합니다.
     */
    List<ProjectStatisticsTargetProjectRow> listPublicProjectsByChapterId(Long chapterId);

    /**
     * 단건 프로젝트의 ACTIVE 프로젝트 멤버를 조회합니다.
     */
    List<ProjectStatisticsMemberRow> listActiveMembersByProjectId(Long projectId);

    /**
     * 공개 프로젝트의 ACTIVE 디자인·개발 팀원을 배정된 차수 ID와 함께 조회합니다. 배정된 차수가 없는 팀원은 제외합니다.
     */
    List<ProjectStatisticsMatchedMemberRow> listPublicActiveMembersByChapterId(Long chapterId);

    /**
     * 프로젝트에 제출된 이후 지원 이력을 조회합니다. DRAFT/CANCELLED 는 지원 현황에서 제외합니다.
     */
    List<ProjectStatisticsApplicationRow> listCountedApplicationsByProjectIds(Collection<Long> projectIds);

}
