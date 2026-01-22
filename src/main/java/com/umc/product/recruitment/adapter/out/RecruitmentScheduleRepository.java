package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.RecruitmentSchedule;
import com.umc.product.recruitment.domain.enums.RecruitmentScheduleType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentScheduleRepository extends JpaRepository<RecruitmentSchedule, Long> {

    List<RecruitmentSchedule> findByRecruitmentId(Long recruitmentId);

    Optional<RecruitmentSchedule> findByRecruitmentIdAndType(Long recruitmentId, RecruitmentScheduleType type);

    List<RecruitmentSchedule> findByRecruitmentIdInAndType(
            List<Long> recruitmentIds,
            RecruitmentScheduleType type
    );
}
