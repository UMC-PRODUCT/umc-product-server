package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadFormSectionPort;
import com.umc.product.survey.application.port.out.SaveFormSectionPort;
import com.umc.product.survey.domain.FormSection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FormSectionPersistenceAdapter implements SaveFormSectionPort, LoadFormSectionPort {

    private final FormSectionJpaRepository formSectionJpaRepository;

    @Override
    public FormSection save(FormSection formSection) {
        return formSectionJpaRepository.save(formSection);
    }

    @Override
    public List<FormSection> saveAll(List<FormSection> sections) {
        return formSectionJpaRepository.saveAll(sections);
    }

    @Override
    public Optional<FormSection> findById(Long sectionId) {
        return formSectionJpaRepository.findById(sectionId);
    }

    @Override
    public List<FormSection> listByFormId(Long formId) {
        return formSectionJpaRepository.findAllByFormId(formId);
    }

    @Override
    public void deleteById(Long sectionId) {
        formSectionJpaRepository.deleteById(sectionId);
    }

    @Override
    public void deleteByFormId(Long formId) {
        formSectionJpaRepository.deleteByFormId(formId);
    }
}
