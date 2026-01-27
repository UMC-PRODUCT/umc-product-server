package com.umc.product.terms.application.service.command;

import com.umc.product.terms.application.port.in.command.ManageTermsAgreementUseCase;
import com.umc.product.terms.application.port.in.command.dto.CreateTermConsentCommand;
import com.umc.product.terms.application.port.out.LoadTermsConsentPort;
import com.umc.product.terms.application.port.out.LoadTermsPort;
import com.umc.product.terms.application.port.out.SaveTermsConsentLogPort;
import com.umc.product.terms.application.port.out.SaveTermsConsentPort;
import com.umc.product.terms.domain.Terms;
import com.umc.product.terms.domain.TermsConsent;
import com.umc.product.terms.domain.TermsConsentLog;
import com.umc.product.terms.domain.enums.TermsConsentStatus;
import com.umc.product.terms.domain.enums.TermsType;
import com.umc.product.terms.domain.exception.TermsDomainException;
import com.umc.product.terms.domain.exception.TermsErrorCode;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TermsAgreementCommandService implements ManageTermsAgreementUseCase {

    private final LoadTermsPort loadTermsPort;
    private final LoadTermsConsentPort loadTermsConsentPort;
    private final SaveTermsConsentPort saveTermsConsentPort;
    private final SaveTermsConsentLogPort saveTermsConsentLogPort;

    @Override
    public void createTermConsent(CreateTermConsentCommand command) {
        // 약관 존재 확인
        Terms terms = loadTermsPort.findById(command.termId())
                .orElseThrow(() -> new TermsDomainException(TermsErrorCode.TERMS_NOT_FOUND));

        if (command.isAgreed()) {
            // 동의 처리
            // 이미 동의했는지 확인
            if (loadTermsConsentPort.existsByMemberIdAndTermType(command.memberId(), terms.getType())) {
                throw new TermsDomainException(TermsErrorCode.TERMS_CONSENT_ALREADY_EXISTS);
            }

            TermsConsent termsConsent = TermsConsent.builder()
                    .memberId(command.memberId())
                    .termType(terms.getType())
                    .agreedAt(Instant.now())
                    .build();

            saveTermsConsentPort.save(termsConsent);

            // 동의 로그 기록
            saveConsentLog(command.memberId(), terms.getType(), TermsConsentStatus.AGREED);
        } else {
            // 동의 철회 처리
            TermsConsent termsConsent = loadTermsConsentPort
                    .findByMemberIdAndTermType(command.memberId(), terms.getType())
                    .orElseThrow(() -> new TermsDomainException(TermsErrorCode.TERMS_CONSENT_NOT_FOUND));

            saveTermsConsentPort.delete(termsConsent);

            // 철회 로그 기록
            saveConsentLog(command.memberId(), terms.getType(), TermsConsentStatus.WITHDRAWN);
        }
    }

    private void saveConsentLog(Long memberId, TermsType termType, TermsConsentStatus status) {
        saveTermsConsentLogPort.save(
                TermsConsentLog.builder()
                        .memberId(memberId)
                        .termType(termType)
                        .status(status)
                        .build()
        );
    }
}
