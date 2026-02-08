package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.InterviewLiveQuestion;

public interface SaveInterviewLiveQuestionPort {

    InterviewLiveQuestion save(InterviewLiveQuestion interviewLiveQuestion);
}
