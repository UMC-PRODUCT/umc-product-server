package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.application.port.out.LoadProjectStatisticsPort;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsApplicationRow;
import com.umc.product.project.application.port.out.dto.ProjectStatisticsMemberRow;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectStatisticsPersistenceAdapter implements LoadProjectStatisticsPort {

    private final ProjectStatisticsQueryRepository queryRepository;

    @Override
    public List<ProjectStatisticsMemberRow> listActiveMembersByProjectId(Long projectId) {
        return queryRepository.listActiveMembersByProjectId(projectId);
    }

    @Override
    public List<ProjectStatisticsMemberRow> listActiveMembersByChapterId(Long chapterId) {
        return queryRepository.listActiveMembersByChapterId(chapterId);
    }

    @Override
    public List<ProjectStatisticsApplicationRow> listCountedApplicationsByProjectIdsAndMemberIds(
        Collection<Long> projectIds,
        Collection<Long> memberIds
    ) {
        return queryRepository.listCountedApplicationsByProjectIdsAndMemberIds(projectIds, memberIds);
    }
}
