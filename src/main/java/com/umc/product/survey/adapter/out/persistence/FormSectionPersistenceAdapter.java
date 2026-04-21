package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.SaveFormSectionPort;
import com.umc.product.survey.domain.FormSection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FormSectionPersistenceAdapter implements SaveFormSectionPort {

    private final FormSectionJpaRepository formSectionJpaRepository;

    @Override
    public FormSection save(FormSection formSection) {
        return formSectionJpaRepository.save(formSection);
    }
}
