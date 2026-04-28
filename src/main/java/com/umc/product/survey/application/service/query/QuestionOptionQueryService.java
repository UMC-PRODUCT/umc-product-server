package com.umc.product.survey.application.service.query;

import com.umc.product.survey.application.port.in.query.GetQuestionOptionUseCase;
import com.umc.product.survey.application.port.in.query.dto.QuestionOptionInfo;
import com.umc.product.survey.application.port.out.LoadQuestionOptionPort;
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
public class QuestionOptionQueryService implements GetQuestionOptionUseCase {

    private final LoadQuestionOptionPort loadQuestionOptionPort;

    @Override
    public Optional<QuestionOptionInfo> findById(Long optionId) {
        return loadQuestionOptionPort.findById(optionId)
            .map(QuestionOptionInfo::from);
    }

    @Override
    public QuestionOptionInfo getById(Long optionId) {
        return loadQuestionOptionPort.findById(optionId)
            .map(QuestionOptionInfo::from)
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.QUESTION_OPTION_NOT_FOUND));
    }

    @Override
    public List<QuestionOptionInfo> listByQuestionId(Long questionId) {
        return loadQuestionOptionPort.listByQuestionId(questionId).stream()
            .map(QuestionOptionInfo::from)
            .toList();
    }
}
