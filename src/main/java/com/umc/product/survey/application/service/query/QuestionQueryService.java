package com.umc.product.survey.application.service.query;

import com.umc.product.survey.application.port.in.query.GetQuestionUseCase;
import com.umc.product.survey.application.port.in.query.dto.QuestionInfo;
import com.umc.product.survey.application.port.out.LoadQuestionPort;
import com.umc.product.survey.domain.exception.SurveyDomainException;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class QuestionQueryService implements GetQuestionUseCase {

    private final LoadQuestionPort loadQuestionPort;

    @Override
    public Optional<QuestionInfo> findById(Long questionId) {
        return loadQuestionPort.findById(questionId)
            .map(QuestionInfo::from);
    }

    @Override
    public QuestionInfo getById(Long questionId) {
        return loadQuestionPort.findById(questionId)
            .map(QuestionInfo::from)
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.QUESTION_NOT_FOUND));
    }

    @Override
    public List<QuestionInfo> listBySectionId(Long sectionId) {
        return loadQuestionPort.listBySectionId(sectionId).stream()
            .map(QuestionInfo::from)
            .toList();
    }
}
