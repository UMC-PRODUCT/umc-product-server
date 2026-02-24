package com.umc.product.term.application.service.query;

import com.umc.product.term.application.port.in.query.GetTermUseCase;
import com.umc.product.term.application.port.in.query.dto.TermInfo;
import com.umc.product.term.application.port.out.LoadTermPort;
import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.enums.TermType;
import com.umc.product.term.domain.exception.TermDomainException;
import com.umc.product.term.domain.exception.TermErrorCode;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermQueryService implements GetTermUseCase {

    private final LoadTermPort loadTermPort;

    @Override
    public TermInfo getTermsByType(TermType type) {
        if (type == null) {
            throw new TermDomainException(TermErrorCode.TERMS_TYPE_REQUIRED);
        }

        Term term = loadTermPort.findActiveByType(type)
            .orElseThrow(() -> new TermDomainException(TermErrorCode.TERMS_NOT_FOUND));

        return TermInfo.from(term);
    }

    @Override
    public TermInfo getTermsById(Long termsId) {
        if (termsId == null) {
            throw new TermDomainException(TermErrorCode.TERM_ID_REQUIRED);
        }

        Term term = loadTermPort.findById(termsId)
            .orElseThrow(() -> new TermDomainException(TermErrorCode.TERMS_NOT_FOUND));

        return TermInfo.from(term);
    }

    @Override
    public Set<Long> getRequiredTermIds() {
        return loadTermPort.findAllActiveRequired().stream()
            .map(Term::getId)
            .collect(Collectors.toSet());
    }
}
