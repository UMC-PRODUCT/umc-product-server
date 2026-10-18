package com.umc.product.project.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.project.domain.ProjectMatchingChapterTransfer;

public interface ProjectMatchingChapterTransferJpaRepository
    extends JpaRepository<ProjectMatchingChapterTransfer, Long> {

    Optional<ProjectMatchingChapterTransfer> findByGisuIdAndMemberIdAndReleasedAtIsNull(Long gisuId, Long memberId);

    @Query("""
        select t
        from ProjectMatchingChapterTransfer t
        where t.releasedAt is null
          and (t.toChapterId = :chapterId or t.fromChapterId = :chapterId)
        """)
    List<ProjectMatchingChapterTransfer> findActiveByChapterId(@Param("chapterId") Long chapterId);
}
