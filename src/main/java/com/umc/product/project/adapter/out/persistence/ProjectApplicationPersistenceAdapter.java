package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.application.port.out.LoadProjectApplicationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectApplicationPersistenceAdapter implements LoadProjectApplicationPort {

    private final ProjectApplicationJpaRepository jpaRepository;

    @Override
    public boolean existsByAppliedMatchingRoundId(Long matchingRoundId) {
        return jpaRepository.existsByAppliedMatchingRound_Id(matchingRoundId);
    }
}
