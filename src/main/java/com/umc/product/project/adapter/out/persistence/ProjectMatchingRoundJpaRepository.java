package com.umc.product.project.adapter.out.persistence;

import com.umc.product.project.domain.ProjectMatchingRound;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectMatchingRoundJpaRepository extends JpaRepository<ProjectMatchingRound, Long> {

    List<ProjectMatchingRound> findAllByChapterIdOrderByStartsAtAsc(Long chapterId);

    List<ProjectMatchingRound> findAllByOrderByStartsAtAsc();

    @Query("""
        select r
        from ProjectMatchingRound r
        where r.chapterId = :chapterId
          and r.startsAt <= :time
          and r.endsAt >= :time
        order by r.startsAt asc
        """)
    List<ProjectMatchingRound> findOpenAt(
        @Param("chapterId") Long chapterId,
        @Param("time") Instant time
    );

    @Query("""
        select r
        from ProjectMatchingRound r
        where r.chapterId = :chapterId
          and r.startsAt <= :decisionDeadline
          and r.decisionDeadline >= :startsAt
        order by r.startsAt asc
        """)
    List<ProjectMatchingRound> findOverlapping(
        @Param("chapterId") Long chapterId,
        @Param("startsAt") Instant startsAt,
        @Param("decisionDeadline") Instant decisionDeadline
    );

    @Query("""
        select r
        from ProjectMatchingRound r
        where r.id <> :id
          and r.chapterId = :chapterId
          and r.startsAt <= :decisionDeadline
          and r.decisionDeadline >= :startsAt
        order by r.startsAt asc
        """)
    List<ProjectMatchingRound> findOverlappingExceptId(
        @Param("id") Long id,
        @Param("chapterId") Long chapterId,
        @Param("startsAt") Instant startsAt,
        @Param("decisionDeadline") Instant decisionDeadline
    );
}
