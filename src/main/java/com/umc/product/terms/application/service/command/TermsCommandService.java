package com.umc.product.terms.application.service.command;

import com.umc.product.terms.application.port.in.command.ManageTermsUseCase;
import com.umc.product.terms.application.port.in.command.dto.CreateTermCommand;
import com.umc.product.terms.application.port.out.SaveTermsPort;
import com.umc.product.terms.domain.Terms;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TermsCommandService implements ManageTermsUseCase {

    private final SaveTermsPort saveTermsPort;

    @Override
    public Long createTerms(CreateTermCommand command) {
        Terms terms = Terms.builder()
                .type(command.type())
                .title(command.title())
                .content(command.content())
                .version(command.version())
                .required(command.required())
                .effectiveDate(command.effectiveDate())
                .build();

        return saveTermsPort.save(terms).getId();
    }
}
