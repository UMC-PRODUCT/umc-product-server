package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.InterviewLiveQuestion;
import java.util.List;
import java.util.Optional;

public interface LoadInterviewLiveQuestionPort {

    int countByApplicationId(Long applicationId);

    List<InterviewLiveQuestion> findByApplicationIdOrderByIdAsc(Long applicationId);

    Optional<InterviewLiveQuestion> findById(Long questionId);
}
