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
            where r.school_id = :schoolId
              and r.status = 'PUBLISHED'
              and r.id <> :excludeRecruitmentId
              -- 1. 해당 모집이 "진행 중"인지 확인 (최종 발표일이 현재 이후)
              and exists (
                  select 1 from recruitment_schedule s_fin
                  where s_fin.recruitment_id = r.id
                    and s_fin.type = 'FINAL_RESULT_AT'
                    and s_fin.starts_at > :now
              )
              -- 2. 충돌 조건 검사
              and (
                  -- (Case A) 뿌리(Root)가 아예 다른 모집인 경우 -> 무조건 충돌
                  r.root_recruitment_id <> :rootId
                  or
                  -- (Case B) 같은 뿌리지만, 서류 접수 기간(APPLY_WINDOW)이 겹치는 경우 -> 충돌
                  (
                      r.root_recruitment_id = :rootId
                      and exists (
                          select 1 from recruitment_schedule s_app
                          where s_app.recruitment_id = r.id
                            and s_app.type = 'APPLY_WINDOW'
                            and s_app.starts_at < :myApplyEnd
                            and s_app.ends_at > :myApplyStart
                      )
                  )
              )
        )
        """, nativeQuery = true)
    boolean existsOtherOngoingPublishedRecruitment(
        @Param("schoolId") Long schoolId,
        @Param("excludeRecruitmentId") Long excludeRecruitmentId,
        @Param("rootId") Long rootId,
        @Param("myApplyStart") Instant myApplyStart,
        @Param("myApplyEnd") Instant myApplyEnd,
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
                    AND s2.startsAt >= :limit
              )
            ORDER BY r.updatedAt DESC
        """)
    List<Long> findActiveRecruitmentIds(
        @Param("schoolId") Long schoolId,
        @Param("gisuId") Long gisuId,
        @Param("now") Instant now,
        @Param("limit") Instant limit
    );
}
