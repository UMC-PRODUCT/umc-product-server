package com.umc.product.term.application.service.command;

import com.umc.product.term.application.port.in.command.ManageTermUseCase;
import com.umc.product.term.application.port.in.command.dto.CreateTermCommand;
import com.umc.product.term.application.port.out.LoadTermPort;
import com.umc.product.term.application.port.out.SaveTermPort;
import com.umc.product.term.domain.Term;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TermCommandService implements ManageTermUseCase {

    private final SaveTermPort saveTermPort;
    private final LoadTermPort loadTermPort;

    @Override
    public Long createTerms(CreateTermCommand command) {
        // 새로 생성하려는 약관과 동일한 타입의 기존 활성 약관을 찾아 비활성화 처리
        // active = false로 변경
        loadTermPort.findActiveByType(command.type())
            .ifPresent(Term::deactivate);

        Term term = Term.builder()
            .type(command.type())
            .link(command.link())
            .required(command.required())
            .build();

        return saveTermPort.save(term).getId();
    }
}
