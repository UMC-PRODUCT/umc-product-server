package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationPort;
import com.umc.product.project.domain.ProjectApplication;
import com.umc.product.project.domain.enums.ProjectApplicationStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectApplicationPersistenceAdapter implements LoadProjectApplicationPort, SaveProjectApplicationPort {

    private final ProjectApplicationJpaRepository projectApplicationJpaRepository;
    private final ProjectApplicationQueryRepository projectApplicationQueryRepository;

    @Override
    public boolean existsByAppliedMatchingRoundId(Long matchingRoundId) {
        return projectApplicationJpaRepository.existsByAppliedMatchingRound_Id(matchingRoundId);
    }

    @Override
    public Optional<ProjectApplication> findByProjectIdAndApplicantMemberIdAndStatus(
        Long projectId,
        Long applicantMemberId,
        ProjectApplicationStatus status
    ) {
        return projectApplicationQueryRepository.findByProjectIdAndApplicantMemberIdAndStatus(
            projectId,
            applicantMemberId,
            status
        );
    }

    @Override
    public ProjectApplication save(ProjectApplication application) {
        return projectApplicationJpaRepository.save(application);
    }
}
