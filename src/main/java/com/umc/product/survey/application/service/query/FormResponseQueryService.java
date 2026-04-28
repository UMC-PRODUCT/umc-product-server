package com.umc.product.survey.application.service.query;

import com.umc.product.survey.application.port.in.query.GetAnswerUseCase;
import com.umc.product.survey.application.port.in.query.GetFormResponseUseCase;
import com.umc.product.survey.application.port.in.query.dto.AnswerInfo;
import com.umc.product.survey.application.port.in.query.dto.FormResponseInfo;
import com.umc.product.survey.application.port.in.query.dto.FormResponseWithAnswersInfo;
import com.umc.product.survey.application.port.out.LoadFormResponsePort;
import com.umc.product.survey.domain.FormResponse;
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
public class FormResponseQueryService implements GetFormResponseUseCase {

    private final LoadFormResponsePort loadFormResponsePort;
    private final GetAnswerUseCase getAnswerUseCase;

    @Override
    public Optional<FormResponseInfo> findById(Long formResponseId) {
        return loadFormResponsePort.findById(formResponseId)
            .map(FormResponseInfo::from);
    }

    @Override
    public FormResponseInfo getById(Long formResponseId) {
        return loadFormResponsePort.findById(formResponseId)
            .map(FormResponseInfo::from)
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.FORM_RESPONSE_NOT_FOUND));
    }

    @Override
    public List<FormResponseInfo> listByFormId(Long formId) {
        return loadFormResponsePort.listByFormId(formId).stream()
            .map(FormResponseInfo::from)
            .toList();
    }

    @Override
    public List<FormResponseInfo> listSubmittedByFormId(Long formId) {
        return loadFormResponsePort.listSubmittedByFormId(formId).stream()
            .map(FormResponseInfo::from)
            .toList();
    }

    @Override
    public List<FormResponseInfo> listDraftByRespondentMemberId(Long respondentMemberId) {
        return loadFormResponsePort.findAllDraftByRespondentMemberId(respondentMemberId).stream()
            .map(FormResponseInfo::from)
            .toList();
    }

    @Override
    public Optional<FormResponseInfo> findDraftByFormIdAndRespondentMemberId(Long formId, Long respondentMemberId) {
        return loadFormResponsePort.findDraftByFormIdAndRespondentMemberId(formId, respondentMemberId)
            .map(FormResponseInfo::from);
    }

    @Override
    public Optional<FormResponseInfo> findSubmittedByFormIdAndRespondentMemberId(Long formId, Long respondentMemberId) {
        return loadFormResponsePort.findSubmittedByFormIdAndRespondentMemberId(formId, respondentMemberId)
            .map(FormResponseInfo::from);
    }

    @Override
    public FormResponseWithAnswersInfo getResponseWithAnswers(Long formResponseId) {
        FormResponse formResponse = loadFormResponsePort.findById(formResponseId)
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.FORM_RESPONSE_NOT_FOUND));

        List<AnswerInfo> answers = getAnswerUseCase.listByFormResponseId(formResponseId);
        return FormResponseWithAnswersInfo.from(formResponse, answers);
    }
}
