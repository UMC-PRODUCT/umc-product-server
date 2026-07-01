package com.umc.product.curriculum.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.curriculum.domain.ChallengerWorkbook;

public interface ChallengerWorkbookJpaRepository extends JpaRepository<ChallengerWorkbook, Long> {

    @Override
    @EntityGraph(attributePaths = {"originalWorkbook", "originalWorkbook.weeklyCurriculum"})
    Optional<ChallengerWorkbook> findById(Long id);

    Optional<ChallengerWorkbook> findByMemberIdAndOriginalWorkbookId(Long memberId, Long originalWorkbookId);

    boolean existsByOriginalWorkbookId(Long originalWorkbookId);

    @EntityGraph(attributePaths = {"originalWorkbook", "originalWorkbook.weeklyCurriculum"})
    List<ChallengerWorkbook> findByMemberIdAndOriginalWorkbookIdIn(Long memberId, List<Long> originalWorkbookIds);

    @EntityGraph(attributePaths = {"originalWorkbook", "originalWorkbook.weeklyCurriculum"})
    List<ChallengerWorkbook> findByMemberIdInAndOriginalWorkbook_WeeklyCurriculum_IdIn(
        List<Long> memberIds,
        List<Long> weeklyCurriculumIds
    );
}
