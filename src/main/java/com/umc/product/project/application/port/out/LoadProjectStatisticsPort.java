package com.umc.product.project.application.port.out;

import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import java.util.Collection;
import java.util.List;

public interface LoadProjectStatisticsPort {

    /**
     * 단건 프로젝트의 ACTIVE 프로젝트 멤버를 조회합니다.
     */
    List<ProjectStatisticsMemberRow> listActiveMembersByProjectId(Long projectId);

    /**
     * 지부 내 전체 프로젝트의 ACTIVE 프로젝트 멤버를 조회합니다.
     */
    List<ProjectStatisticsMemberRow> listActiveMembersByChapterId(Long chapterId);

    /**
     * 프로젝트와 멤버 조합에 해당하는 제출 이후 지원 이력을 조회합니다. DRAFT/CANCELLED 는 지원 현황에서 제외합니다.
     */
    List<ProjectStatisticsApplicationRow> listCountedApplicationsByProjectIdsAndMemberIds(
        Collection<Long> projectIds,
        Collection<Long> memberIds
    );
}
