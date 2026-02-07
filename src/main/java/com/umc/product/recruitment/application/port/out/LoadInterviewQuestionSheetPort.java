package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.InterviewQuestionSheet;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.Optional;

public interface LoadInterviewQuestionSheetPort {

    Optional<InterviewQuestionSheet> findTopByRecruitmentAndPartKeyOrderByOrderNoDesc(Recruitment recruitment,
                                                                                      PartKey partKey);
}
