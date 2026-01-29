package com.umc.product.terms.application.service.query;

import com.umc.product.terms.application.port.in.query.GetTermsUseCase;
import com.umc.product.terms.application.port.in.query.dto.TermsInfo;
import com.umc.product.terms.application.port.out.LoadTermsPort;
import com.umc.product.terms.domain.Terms;
import com.umc.product.terms.domain.enums.TermsType;
import com.umc.product.terms.domain.exception.TermsDomainException;
import com.umc.product.terms.domain.exception.TermsErrorCode;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsQueryService implements GetTermsUseCase {

    private final LoadTermsPort loadTermsPort;

    @Override
    public TermsInfo getTermsByType(TermsType type) {
        if (type == null) {
            throw new TermsDomainException(TermsErrorCode.TERMS_TYPE_REQUIRED);
        }

        Terms terms = loadTermsPort.findActiveByType(type)
                .orElseThrow(() -> new TermsDomainException(TermsErrorCode.TERMS_NOT_FOUND));

        return new TermsInfo(
                terms.getId(),
                terms.getTitle(),
                terms.getContent(),
                terms.isRequired(),
                terms.getType(),
                terms.getEffectiveDate()
        );
    }

    @Override
    public TermsInfo getTermsById(Long termsId) {
        if (termsId == null) {
            throw new TermsDomainException(TermsErrorCode.TERM_ID_REQUIRED);
        }

        Terms terms = loadTermsPort.findById(termsId)
                .orElseThrow(() -> new TermsDomainException(TermsErrorCode.TERMS_NOT_FOUND));

        return new TermsInfo(
                terms.getId(),
                terms.getTitle(),
                terms.getContent(),
                terms.isRequired(),
                terms.getType(),
                terms.getEffectiveDate()
        );
    }

    @Override
    public Set<Long> getRequiredTermIds() {
        return loadTermsPort.findAllActiveRequired().stream()
                .map(Terms::getId)
                .collect(Collectors.toSet());
    }
}
