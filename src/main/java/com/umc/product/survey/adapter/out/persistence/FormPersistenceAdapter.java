package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.LoadFormPort;
import com.umc.product.survey.application.port.out.SaveFormPort;
import com.umc.product.survey.domain.Form;
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
    public void deleteById(Long formId) {
        formJpaRepository.deleteById(formId);
    }
}
