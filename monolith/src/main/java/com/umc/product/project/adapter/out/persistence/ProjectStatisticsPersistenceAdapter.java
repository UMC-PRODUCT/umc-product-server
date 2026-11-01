package com.umc.product.project.adapter.out.persistence;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.umc.product.project.application.port.out.LoadProjectStatisticsPort;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMatchedMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsProjectRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsTargetProjectRow;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

import lombok.RequiredArgsConstructor;

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
    public List<ProjectStatisticsTargetProjectRow> listApplicationStatisticsProjectsByIds(Collection<Long> projectIds) {
        return queryRepository.listApplicationStatisticsProjectsByIds(projectIds);
    }

    @Override
    public List<ProjectStatisticsTargetProjectRow> listPublicProjectsByChapterId(Long chapterId) {
        return queryRepository.listPublicProjectsByChapterId(chapterId);
    }

    @Override
    public List<ProjectStatisticsMemberRow> listActiveMembersByProjectId(Long projectId) {
        return queryRepository.listActiveMembersByProjectId(projectId);
    }

    @Override
    public List<ProjectStatisticsMatchedMemberRow> listPublicActiveMembersByChapterId(Long chapterId) {
        return queryRepository.listPublicActiveMembersByChapterId(chapterId);
    }

    @Override
    public List<ProjectStatisticsApplicationRow> listCountedApplicationsByProjectIds(Collection<Long> projectIds) {
        return queryRepository.listCountedApplicationsByProjectIds(projectIds);
    }

}
