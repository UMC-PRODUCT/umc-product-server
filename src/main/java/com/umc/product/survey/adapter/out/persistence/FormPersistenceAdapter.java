package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.SaveFormPort;
import com.umc.product.survey.domain.Form;
import com.umc.product.survey.domain.exception.SurveyDomainException;
import com.umc.product.survey.domain.exception.SurveyErrorCode;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
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
    public List<Form> batchGetByIds(Collection<Long> formIds) {
        if (formIds == null || formIds.isEmpty()) {
            return List.of();
        }

        List<Long> uniqueIds = formIds.stream()
            .collect(java.util.stream.Collectors.collectingAndThen(
                java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                List::copyOf
            ));
        if (uniqueIds.isEmpty()) {
            return List.of();
        }

        List<Form> forms = formJpaRepository.findAllById(uniqueIds);
        if (forms.size() != uniqueIds.size()) {
            throw new SurveyDomainException(SurveyErrorCode.SURVEY_NOT_FOUND);
        }
        return forms;
    }

    @Override
    public void deleteById(Long formId) {
        formJpaRepository.deleteById(formId);
    }
}
