package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.InterviewLiveQuestion;
import java.util.Optional;

public interface LoadInterviewLiveQuestionPort {

    int countByApplicationId(Long applicationId);


    Optional<InterviewLiveQuestion> findById(Long questionId);
}
