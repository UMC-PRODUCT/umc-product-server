package com.umc.product.project.application.port.out;

import com.umc.product.project.domain.ProjectMatchingRound;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LoadProjectMatchingRoundPort {

    ProjectMatchingRound getById(Long id);

    Optional<ProjectMatchingRound> findById(Long id);

    List<ProjectMatchingRound> listByIds(List<Long> ids);

    List<ProjectMatchingRound> batchGetByIds(List<Long> ids);

    List<ProjectMatchingRound> listByChapterId(Long chapterId);

    List<ProjectMatchingRound> listAll();

    List<ProjectMatchingRound> listOpenAt(Long chapterId, Instant time);

    List<ProjectMatchingRound> listOverlapping(Long chapterId, Instant startsAt, Instant decisionDeadline);

    List<ProjectMatchingRound> listOverlappingExceptId(
        Long id, Long chapterId, Instant startsAt, Instant decisionDeadline
    );
}
