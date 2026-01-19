package com.umc.product.terms.application.service.query;

import com.umc.product.terms.application.port.in.query.GetTermsAgreementUseCase;
import com.umc.product.terms.application.port.in.query.dto.TermsInfo;
import com.umc.product.terms.application.port.out.LoadTermsConsentPort;
import com.umc.product.terms.application.port.out.LoadTermsPort;
import com.umc.product.terms.domain.Terms;
import com.umc.product.terms.domain.TermsConsent;
import com.umc.product.terms.domain.exception.TermsDomainException;
import com.umc.product.terms.domain.exception.TermsErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsAgreementQueryService implements GetTermsAgreementUseCase {

    private final LoadTermsConsentPort loadTermsConsentPort;
    private final LoadTermsPort loadTermsPort;

    @Override
    public List<TermsInfo> getAgreedTermsByMemberId(Long memberId) {
        if (memberId == null) {
            throw new TermsDomainException(TermsErrorCode.MEMBER_ID_REQUIRED);
        }

        List<TermsConsent> consents = loadTermsConsentPort.findByMemberId(memberId);

        return consents.stream()
                .map(consent -> {
                    Terms terms = loadTermsPort.findActiveByType(consent.getTermType())
                            .orElseThrow(() -> new TermsDomainException(TermsErrorCode.TERMS_NOT_FOUND));

                    return new TermsInfo(
                            terms.getId(),
                            terms.getTitle(),
                            terms.getContent(),
                            terms.isRequired(),
                            terms.getType(),
                            terms.getEffectiveDate()
                    );
                })
                .toList();
    }
}
