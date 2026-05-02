package com.umc.product.survey.application.service.query;

import com.umc.product.survey.application.port.in.query.GetFormSectionUseCase;
import com.umc.product.survey.application.port.in.query.dto.FormSectionInfo;
import com.umc.product.survey.application.port.out.LoadFormSectionPort;
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
public class FormSectionQueryService implements GetFormSectionUseCase {

    private final LoadFormSectionPort loadFormSectionPort;

    @Override
    public Optional<FormSectionInfo> findById(Long sectionId) {
        return loadFormSectionPort.findById(sectionId)
            .map(FormSectionInfo::from);
    }

    @Override
    public FormSectionInfo getById(Long sectionId) {
        return loadFormSectionPort.findById(sectionId)
            .map(FormSectionInfo::from)
            .orElseThrow(() -> new SurveyDomainException(SurveyErrorCode.SURVEY_NOT_FOUND));
    }

    @Override
    public List<FormSectionInfo> listByFormId(Long formId) {
        return loadFormSectionPort.listByFormId(formId).stream()
            .map(FormSectionInfo::from)
            .toList();
    }
}
