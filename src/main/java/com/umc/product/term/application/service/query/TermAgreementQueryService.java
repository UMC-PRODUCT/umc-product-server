package com.umc.product.term.application.service.query;

import com.umc.product.term.application.port.in.query.GetTermAgreementUseCase;
import com.umc.product.term.application.port.in.query.dto.TermInfo;
import com.umc.product.term.application.port.out.LoadTermConsentPort;
import com.umc.product.term.application.port.out.LoadTermPort;
import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.TermConsent;
import com.umc.product.term.domain.enums.TermType;
import com.umc.product.term.domain.exception.TermDomainException;
import com.umc.product.term.domain.exception.TermErrorCode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 동의한 항목들의 타입(SERVICE, PRIVACY 등)만 리스트로 뽑기
        List<TermType> types = consents.stream().map(TermConsent::getTermType).toList();

        // 해당 타입들의 활성 약관 상세 정보 가져오기
        List<Term> activeTermList = loadTermPort.findAllActiveByTypes(types);

        // Map<타입, 약관객체> 형태로 변환 (중복 발생 시 최근 거 선택)
        Map<TermType, Term> termsMap = activeTermList.stream()
            .collect(Collectors.toMap(Term::getType, terms -> terms, (oldValue, newValue) -> newValue));

        // 동의 내역을 순회하며 Map에서 데이터를 꺼내 TermsInfo 만들기
        return consents.stream()
            .map(consent -> {
                Term term = termsMap.get(consent.getTermType());
                if (term == null) {
                    throw new TermDomainException(TermErrorCode.TERMS_NOT_FOUND);
                }

                return TermInfo.from(term);
            })
            .toList();
    }
}
