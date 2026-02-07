package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.Application;
import java.util.List;
import java.util.Optional;

public interface LoadApplicationPort {
    Optional<Application> findByRecruitmentIdAndApplicantId(Long recruitmentId, Long memberId);

    Optional<Application> findById(Long applicationId);

    boolean existsByRecruitmentId(Long recruitmentId);

    boolean existsByRecruitmentIdAndApplicantMemberId(Long recruitmentId, Long applicantMemberId);

    Optional<Application> findByRecruitmentIdAndApplicantMemberId(Long recruitmentId, Long applicantMemberId);

    List<Application> findAllByApplicantMemberId(Long applicantMemberId);

    Optional<Application> getByRecruitmentIdAndApplicationId(Long recruitmentId, Long applicationId);
}
