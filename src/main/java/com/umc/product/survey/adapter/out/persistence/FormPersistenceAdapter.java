package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.application.port.out.SaveFormPort;
import com.umc.product.survey.domain.Form;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FormPersistenceAdapter implements SaveFormPort {

    private final FormJpaRepository formJpaRepository;

    @Override
    public Form save(Form form) {
        return formJpaRepository.save(form);
    }
}
