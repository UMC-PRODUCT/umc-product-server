package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.Application;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByRecruitmentIdAndApplicantMemberId(Long recruitmentId, Long applicantMemberId);

    List<Application> findAllByRecruitmentId(Long recruitmentId);

    long countByRecruitmentId(Long recruitmentId);

    boolean existsByRecruitmentId(Long recruitmentId);

    boolean existsByRecruitmentIdAndApplicantMemberId(Long recruitmentId, Long applicantMemberId);

    List<Application> findAllByApplicantMemberId(Long applicantMemberId);

    Optional<Application> findByRecruitmentIdAndId(Long recruitmentId, Long applicationId);
}
