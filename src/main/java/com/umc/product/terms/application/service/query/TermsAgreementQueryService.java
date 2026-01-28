package com.umc.product.terms.application.service.query;

import com.umc.product.terms.application.port.in.query.GetTermsAgreementUseCase;
import com.umc.product.terms.application.port.in.query.dto.TermsInfo;
import com.umc.product.terms.application.port.out.LoadTermsConsentPort;
import com.umc.product.terms.application.port.out.LoadTermsPort;
import com.umc.product.terms.domain.Terms;
import com.umc.product.terms.domain.TermsConsent;
import com.umc.product.terms.domain.enums.TermsType;
import com.umc.product.terms.domain.exception.TermsDomainException;
import com.umc.product.terms.domain.exception.TermsErrorCode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

        // 사용자의 동의 내역 가져오기
        List<TermsConsent> consents = loadTermsConsentPort.findByMemberId(memberId);

        // 동의한 항목들의 타입(SERVICE, PRIVACY 등)만 리스트로 뽑기
        List<TermsType> types = consents.stream().map(TermsConsent::getTermType).toList();

        // 해당 타입들의 활성 약관 상세 정보 가져오기
        List<Terms> activeTermsList = loadTermsPort.findAllActiveByTypes(types);

        // Map<타입, 약관객체> 형태로 변환 (중복 발생 시 최근 거 선택)
        Map<TermsType, Terms> termsMap = activeTermsList.stream()
                .collect(Collectors.toMap(Terms::getType, terms -> terms, (oldValue, newValue) -> newValue));

        // 동의 내역을 순회하며 Map에서 데이터를 꺼내 TermsInfo 만들기
        return consents.stream()
                .map(consent -> {
                    Terms terms = termsMap.get(consent.getTermType());
                    if (terms == null) {
                        throw new TermsDomainException(TermsErrorCode.TERMS_NOT_FOUND);
                    }

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
