package com.umc.product.project.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.umc.product.project.domain.ProjectMatchingRound;

import jakarta.persistence.LockModeType;

public interface ProjectMatchingRoundJpaRepository extends JpaRepository<ProjectMatchingRound, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ProjectMatchingRound r where r.id = :id")
    Optional<ProjectMatchingRound> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select r from ProjectMatchingRound r
        where r.resultPublishAt is not null and r.resultPublishedAt is null
        order by r.resultPublishAt, r.id
        """)
    List<ProjectMatchingRound> findAllNotPublished();

    List<ProjectMatchingRound> findAllByChapterIdOrderByStartsAtAsc(Long chapterId);

    List<ProjectMatchingRound> findAllByOrderByStartsAtAsc();

    @Query("""
        select r
        from ProjectMatchingRound r
        where r.chapterId = :chapterId
          and r.startsAt <= :time
          and r.endsAt >= :time
          and r.phase <> com.umc.product.project.domain.enums.MatchingPhase.FOURTH
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

    List<ProjectMatchingRound> findAllByAutoDecisionExecutedAtIsNullOrderByDecisionDeadlineAsc();
}
