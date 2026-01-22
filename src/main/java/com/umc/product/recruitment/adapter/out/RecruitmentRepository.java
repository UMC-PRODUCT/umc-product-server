package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.Recruitment;
import java.time.Instant;
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
}
