package com.umc.product.terms.application.service.command;

import com.umc.product.terms.application.port.in.command.ManageTermsUseCase;
import com.umc.product.terms.application.port.in.command.dto.CreateTermCommand;
import com.umc.product.terms.application.port.out.LoadTermsPort;
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
    private final LoadTermsPort loadTermsPort;

    @Override
    public Long createTerms(CreateTermCommand command) {
        // 새로 생성하려는 약관과 동일한 타입의 기존 활성 약관을 찾아 비활성화 처리
        // active = false로 변경
        loadTermsPort.findActiveByType(command.type())
            .ifPresent(Terms::deactivate);

        Terms terms = Terms.builder()
            .type(command.type())
            .link(command.link())
            .required(command.required())
            .build();

        return saveTermsPort.save(terms).getId();
    }
}
