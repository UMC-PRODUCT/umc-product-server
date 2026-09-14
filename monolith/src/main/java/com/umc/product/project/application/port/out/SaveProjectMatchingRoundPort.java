package com.umc.product.project.application.port.out;

import java.util.List;

import com.umc.product.project.domain.ProjectMatchingRound;

public interface SaveProjectMatchingRoundPort {

    ProjectMatchingRound save(ProjectMatchingRound matchingRound);

    List<ProjectMatchingRound> saveAll(List<ProjectMatchingRound> matchingRounds);

    void delete(ProjectMatchingRound matchingRound);

    void deleteAll(List<ProjectMatchingRound> matchingRounds);
}
