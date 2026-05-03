package com.umc.product.project.application.port.out;

public interface LoadProjectApplicationPort {

    boolean existsByAppliedMatchingRoundId(Long matchingRoundId);
}
