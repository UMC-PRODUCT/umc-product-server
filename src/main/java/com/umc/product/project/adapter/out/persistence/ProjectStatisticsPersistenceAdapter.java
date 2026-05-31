package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.application.port.out.LoadProjectStatisticsPort;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMatchingRoundRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsProjectRow;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectStatisticsPersistenceAdapter implements LoadProjectStatisticsPort {

    private final ProjectStatisticsQueryRepository queryRepository;

    @Override
    public ProjectStatisticsProjectRow getProjectById(Long projectId) {
        return queryRepository.findProjectById(projectId)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

    @Override
    public List<ProjectStatisticsProjectRow> listProjectsByChapterId(Long chapterId) {
        return queryRepository.listProjectsByChapterId(chapterId);
    }

    @Override
    public List<ProjectStatisticsMatchingRoundRow> listMatchingRoundsByChapterId(Long chapterId) {
        return queryRepository.listMatchingRoundsByChapterId(chapterId);
    }

    @Override
    public List<ProjectStatisticsMemberRow> listActiveMembersByProjectId(Long projectId) {
        return queryRepository.listActiveMembersByProjectId(projectId);
    }

    @Override
    public List<ProjectStatisticsMemberRow> listActiveMembersByChapterId(Long chapterId) {
        return queryRepository.listActiveMembersByChapterId(chapterId);
    }

    @Override
    public List<ProjectStatisticsApplicationRow> listCountedApplicationsByProjectIds(Collection<Long> projectIds) {
        return queryRepository.listCountedApplicationsByProjectIds(projectIds);
    }
}
