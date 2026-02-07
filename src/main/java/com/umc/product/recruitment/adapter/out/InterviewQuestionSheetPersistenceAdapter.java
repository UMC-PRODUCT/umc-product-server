package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.LoadInterviewQuestionSheetPort;
import com.umc.product.recruitment.application.port.out.SaveInterviewQuestionSheetPort;
import com.umc.product.recruitment.domain.InterviewQuestionSheet;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;
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

    @Override
    public List<InterviewQuestionSheet> findByRecruitmentAndPartKey(Recruitment recruitment, PartKey partKey) {
        return interviewQuestionSheetRepository.findByRecruitmentAndPartKey(recruitment, partKey);
    }

    @Override
    public Optional<InterviewQuestionSheet> findById(Long interviewQuestionSheetId) {
        return interviewQuestionSheetRepository.findById(interviewQuestionSheetId);
    }
}
