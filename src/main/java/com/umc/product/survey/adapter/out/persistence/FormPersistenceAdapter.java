package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.survey.application.port.in.query.dto.FormDefinitionInfo;
import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.SaveFormPort;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FormPersistenceAdapter implements SaveFormPort, LoadFormPort {

    private final FormJpaRepository formJpaRepository;

    @Override
    public Form save(Form form) {
        return formJpaRepository.save(form);
    }

    @Override
    public Optional<Form> findById(Long formId) {
        return formJpaRepository.findById(formId);
    }

    @Override
    public FormDefinitionInfo loadFormDefinition(Long formId) {
        Form form = formJpaRepository.findDefinitionById(formId)
                .orElseThrow(() -> new BusinessException(Domain.SURVEY, SurveyErrorCode.SURVEY_NOT_DRAFT));

        return FormDefinitionInfo.from(form);
    }

    @Override
    public void deleteById(Long formId) {
        formJpaRepository.deleteById(formId);
    }
}
