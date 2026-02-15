package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.enums.RecruitmentStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {
    @Query(value = """
        select exists (
            select 1
            from recruitment r
            join recruitment_schedule s
              on s.recruitment_id = r.id
            where r.school_id = :schoolId
              and r.status = 'PUBLISHED'
              and r.id <> :excludeRecruitmentId
              and s.type = 'FINAL_RESULT_AT'
              and s.starts_at > :now
        )
        """, nativeQuery = true)
    boolean existsOtherOngoingPublishedRecruitment(
        @Param("schoolId") Long schoolId,
        @Param("excludeRecruitmentId") Long excludeRecruitmentId,
        @Param("now") Instant now
    );

    List<Recruitment> findByStatus(RecruitmentStatus status);

    @Query(value = """
        select r.id
        from recruitment r
        join recruitment_schedule s
          on s.recruitment_id = r.id
        where r.school_id = :schoolId
          and r.gisu_id = :gisuId
          and r.status = 'PUBLISHED'
          and s.type = 'FINAL_RESULT_AT'
          and s.starts_at >= :now
        order by s.starts_at desc
        limit 1
        """, nativeQuery = true)
    Optional<Long> findActiveRecruitmentId(
        @Param("schoolId") Long schoolId,
        @Param("gisuId") Long gisuId,
        @Param("now") Instant now
    );

    Optional<Recruitment> findByFormId(Long formId);

    @Query("""
          select r.id
          from Recruitment r
          where r.schoolId = :schoolId
            and r.gisuId = :gisuId
            and r.status = com.umc.product.recruitment.domain.enums.RecruitmentStatus.PUBLISHED
          order by r.updatedAt desc
        """)
    List<Long> findLatestPublishedId(Long schoolId, Long gisuId);

    List<Recruitment> findBySchoolIdAndStatus(Long schoolId, RecruitmentStatus status);

    List<Recruitment> findAllBySchoolIdAndGisuIdAndStatus(Long schoolId, Long gisuId, RecruitmentStatus status);

    @Query("""
            SELECT r.id
            FROM Recruitment r
            WHERE r.schoolId = :schoolId
              AND r.gisuId = :gisuId
              AND r.status = com.umc.product.recruitment.domain.enums.RecruitmentStatus.PUBLISHED
              AND EXISTS (
                  SELECT s1
                  FROM RecruitmentSchedule s1
                  WHERE s1.recruitmentId = r.id
                    AND s1.type = 'APPLY_WINDOW'
                    AND s1.startsAt <= :now
              )
              AND EXISTS (
                  SELECT s2
                  FROM RecruitmentSchedule s2
                  WHERE s2.recruitmentId = r.id
                    AND s2.type = 'FINAL_RESULT_AT'
                    AND FUNCTION('DATE_ADD', s2.startsAt, 1, 'DAY') >= :now
              )
            ORDER BY r.updatedAt DESC
        """)
    List<Long> findActiveRecruitmentIds(
        @Param("schoolId") Long schoolId,
        @Param("gisuId") Long gisuId,
        @Param("now") Instant now
    );
}
