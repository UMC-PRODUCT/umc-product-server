package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.Application;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByRecruitmentIdAndApplicantMemberId(Long recruitmentId, Long applicantMemberId);


    @Query("select count(a) from Application a where a.recruitment.id = :recruitmentId")
    long countByRecruitmentId(@Param("recruitmentId") Long recruitmentId);

    boolean existsByRecruitmentId(Long recruitmentId);
}
