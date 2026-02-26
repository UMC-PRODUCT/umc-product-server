package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.InterviewQuestionSheet;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LoadInterviewQuestionSheetPort {

    Optional<InterviewQuestionSheet> findTopByRecruitmentAndPartKeyOrderByOrderNoDesc(Recruitment recruitment,
                                                                                      PartKey partKey);

    List<InterviewQuestionSheet> findByRecruitmentAndPartKey(Recruitment recruitment, PartKey partKey);

    List<InterviewQuestionSheet> findByRecruitmentIdAndPartKeyOrderByOrderNo(Long recruitmentId, PartKey partKey);

    Optional<InterviewQuestionSheet> findById(Long interviewQuestionSheetId);

    List<InterviewQuestionSheet> findByRecruitmentIdAndPartKeyOrderByOrderNoAsc(Long recruitmentId, PartKey partKey);

    List<InterviewQuestionSheet> findByRecruitmentIdAndPartKeysOrderByOrderNoAsc(Long recruitmentId,
                                                                                 Set<PartKey> partKeys);
}
