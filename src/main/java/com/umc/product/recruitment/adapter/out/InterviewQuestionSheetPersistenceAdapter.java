package com.umc.product.recruitment.adapter.out;

import com.umc.product.recruitment.application.port.out.LoadInterviewQuestionSheetPort;
import com.umc.product.recruitment.application.port.out.SaveInterviewQuestionSheetPort;
import com.umc.product.recruitment.domain.InterviewQuestionSheet;
import com.umc.product.recruitment.domain.Recruitment;
import com.umc.product.recruitment.domain.enums.PartKey;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewQuestionSheetPersistenceAdapter implements SaveInterviewQuestionSheetPort,
    LoadInterviewQuestionSheetPort {

    private final InterviewQuestionSheetRepository interviewQuestionSheetRepository;
    private final InterviewQuestionSheetQueryRepository interviewQuestionSheetQueryRepository;

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
    public List<InterviewQuestionSheet> findByRecruitmentIdAndPartKeyOrderByOrderNo(Long recruitmentId,
                                                                                    PartKey partKey) {
        return interviewQuestionSheetRepository.findByRecruitmentIdAndPartKeyOrderByOrderNoAsc(recruitmentId, partKey);
    }

    @Override
    public Optional<InterviewQuestionSheet> findById(Long interviewQuestionSheetId) {
        return interviewQuestionSheetRepository.findById(interviewQuestionSheetId);
    }

    @Override
    public List<InterviewQuestionSheet> findByRecruitmentIdAndPartKeyOrderByOrderNoAsc(
        Long recruitmentId, PartKey partKey
    ) {
        return interviewQuestionSheetRepository.findByRecruitmentIdAndPartKeyOrderByOrderNoAsc(recruitmentId, partKey);
    }

    @Override
    public List<InterviewQuestionSheet> findByRecruitmentIdAndPartKeysOrderByOrderNoAsc(
        Long recruitmentId, Set<PartKey> partKeys
    ) {
        return interviewQuestionSheetQueryRepository.findByRecruitmentIdAndPartKeysOrderByOrderNoAsc(
            recruitmentId, partKeys
        );
    }

    // ================ SaveInterviewQuestionSheetPort ================
    @Override
    public InterviewQuestionSheet save(InterviewQuestionSheet interviewQuestionSheet) {
        return interviewQuestionSheetRepository.save(interviewQuestionSheet);
    }

    @Override
    public void deleteById(Long interviewQuestionSheetId) {
        interviewQuestionSheetRepository.deleteById(interviewQuestionSheetId);
    }
}
