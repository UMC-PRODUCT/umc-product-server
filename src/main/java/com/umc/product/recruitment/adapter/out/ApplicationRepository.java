package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.Application;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByRecruitmentIdAndApplicantMemberId(Long recruitmentId, Long applicantMemberId);
}
