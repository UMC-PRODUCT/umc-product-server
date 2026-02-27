package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.domain.InterviewQuestionSheet;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewQuestionSheetRepository extends JpaRepository<InterviewQuestionSheet, Long> {
    Optional<InterviewQuestionSheet> findTopByRecruitmentAndPartKeyOrderByOrderNoDesc(Recruitment recruitment,
                                                                                      PartKey partKey);

    List<InterviewQuestionSheet> findByRecruitmentAndPartKey(Recruitment recruitment, PartKey partKey);

    List<InterviewQuestionSheet> findByRecruitmentIdAndPartKeyOrderByOrderNoAsc(Long recruitmentId, PartKey partKey);
}
