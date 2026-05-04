package com.umc.product.project.adapter.out.persistence;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.out.LoadProjectMemberPort;
import com.umc.product.project.application.port.out.SaveProjectMemberPort;
import com.umc.product.project.domain.ProjectMember;
import com.umc.product.project.domain.enums.ProjectMemberStatus;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectMemberPersistenceAdapter implements LoadProjectMemberPort, SaveProjectMemberPort {

    private final ProjectMemberJpaRepository repository;

    @Override
    public ProjectMember save(ProjectMember member) {
        return repository.save(member);
    }

    @Override
    public void hardDelete(Long projectMemberId) {
        repository.deleteById(projectMemberId);
    }

    @Override
    public List<ProjectMember> listByProjectId(Long projectId) {
        return repository.findByProjectIdAndStatus(projectId, ProjectMemberStatus.ACTIVE);
    }

    @Override
    public List<ProjectMember> listByProjectIdAndPart(Long projectId, ChallengerPart part) {
        return repository.findByProjectIdAndPartAndStatus(projectId, part, ProjectMemberStatus.ACTIVE);
    }

    @Override
    public Map<ChallengerPart, Long> countByProjectIdGroupByPart(Long projectId) {
        List<Object[]> rows = repository.countByProjectIdGroupByPartRaw(projectId, ProjectMemberStatus.ACTIVE);

        Map<ChallengerPart, Long> result = new EnumMap<>(ChallengerPart.class);
        for (Object[] row : rows) {
            result.put((ChallengerPart) row[0], (Long) row[1]);
        }
        return result;
    }

    @Override
    public Optional<ProjectMember> findByProjectIdAndMemberId(Long projectId, Long memberId) {
        return repository.findByProjectIdAndMemberId(projectId, memberId);
    }
}
