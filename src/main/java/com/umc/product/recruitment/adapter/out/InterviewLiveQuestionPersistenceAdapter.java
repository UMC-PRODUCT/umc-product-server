package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.LoadInterviewLiveQuestionPort;
import com.umc.product.recruitment.application.port.out.SaveInterviewLiveQuestionPort;
import com.umc.product.recruitment.domain.InterviewLiveQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewLiveQuestionPersistenceAdapter implements LoadInterviewLiveQuestionPort,
    SaveInterviewLiveQuestionPort {

    private final InterviewLiveQuestionRepository interviewLiveQuestionRepository;

    // ============ LoadInterviewLiveQuestionPort ============
    @Override
    public int countByApplicationId(Long applicationId) {
        return interviewLiveQuestionRepository.countByApplicationId(applicationId);
    }

    // ============ SaveInterviewLiveQuestionPort ============
    @Override
    public InterviewLiveQuestion save(InterviewLiveQuestion interviewLiveQuestion) {
        return interviewLiveQuestionRepository.save(interviewLiveQuestion);
    }
}
