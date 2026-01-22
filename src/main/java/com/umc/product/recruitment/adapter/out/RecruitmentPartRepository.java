package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.RecruitmentPart;
import com.umc.product.recruitment.domain.enums.RecruitmentPartStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecruitmentPartRepository extends JpaRepository<RecruitmentPart, Long> {
    List<RecruitmentPart> findByRecruitmentId(Long recruitmentId);

    void deleteAllByRecruitmentId(Long recruitmentId);

    List<RecruitmentPart> findByRecruitmentIdAndStatus(Long recruitmentId, RecruitmentPartStatus status);
}
