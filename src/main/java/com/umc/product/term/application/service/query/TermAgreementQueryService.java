package com.umc.product.term.application.service.query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.term.application.port.in.query.GetTermAgreementUseCase;
import com.umc.product.term.application.port.in.query.dto.TermInfo;
import com.umc.product.term.application.port.out.LoadTermConsentPort;
import com.umc.product.term.application.port.out.LoadTermPort;
import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.TermConsent;
import com.umc.product.term.domain.exception.TermDomainException;
import com.umc.product.term.domain.exception.TermErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermAgreementQueryService implements GetTermAgreementUseCase {

    private final LoadTermConsentPort loadTermConsentPort;
    private final LoadTermPort loadTermPort;

    @Override
    public List<TermInfo> getAgreedTermsByMemberId(Long memberId) {
        if (memberId == null) {
            throw new TermDomainException(TermErrorCode.MEMBER_ID_REQUIRED);
        }

        // 사용자의 동의 내역 가져오기
        List<TermConsent> consents = loadTermConsentPort.findByMemberId(memberId);

        // 동의한 약관 row ID만 리스트로 뽑기
        List<Long> termIds = consents.stream().map(TermConsent::getTermId).toList();

        if (termIds.isEmpty()) {
            return List.of();
        }

        // 동의 당시의 약관 row 를 그대로 조회한다. 현재 활성 약관으로 재해석하지 않는다.
        List<Term> terms = loadTermPort.listByIds(termIds);
        Map<Long, Term> termsMap = terms.stream()
            .collect(Collectors.toMap(Term::getId, term -> term));

        // 동의 내역을 순회하며 Map에서 데이터를 꺼내 TermsInfo 만들기
        return consents.stream()
            .map(consent -> {
                Term term = termsMap.get(consent.getTermId());
                if (term == null) {
                    throw new TermDomainException(TermErrorCode.TERMS_NOT_FOUND);
                }

                return TermInfo.from(term);
            })
            .toList();
    }
}
