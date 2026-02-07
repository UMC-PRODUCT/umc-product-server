package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.LoadInterviewQuestionSheetPort;
import com.umc.product.recruitment.application.port.out.SaveInterviewQuestionSheetPort;
import com.umc.product.recruitment.domain.InterviewQuestionSheet;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewQuestionSheetPersistenceAdapter implements SaveInterviewQuestionSheetPort,
    LoadInterviewQuestionSheetPort {

    private final InterviewQuestionSheetRepository interviewQuestionSheetRepository;

    // ================ SaveInterviewQuestionSheetPort ================
    @Override
    public InterviewQuestionSheet save(InterviewQuestionSheet interviewQuestionSheet) {
        return interviewQuestionSheetRepository.save(interviewQuestionSheet);
    }

    // ================ LoadInterviewQuestionSheetPort ================
    @Override
    public Optional<InterviewQuestionSheet> findTopByRecruitmentAndPartKeyOrderByOrderNoDesc(Recruitment recruitment,
                                                                                             PartKey partKey) {
        return interviewQuestionSheetRepository.findTopByRecruitmentAndPartKeyOrderByOrderNoDesc(recruitment, partKey);
    }
}
