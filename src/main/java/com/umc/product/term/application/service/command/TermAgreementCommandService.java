package com.umc.product.term.application.service.command;

import com.umc.product.term.application.port.in.command.ManageTermAgreementUseCase;
import com.umc.product.term.application.port.in.command.dto.CreateTermConsentCommand;
import com.umc.product.term.application.port.out.LoadTermConsentPort;
import com.umc.product.term.application.port.out.LoadTermPort;
import com.umc.product.term.application.port.out.SaveTermConsentLogPort;
import com.umc.product.term.application.port.out.SaveTermConsentPort;
import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.TermConsent;
import com.umc.product.term.domain.TermConsentLog;
import com.umc.product.term.domain.enums.TermConsentStatus;
import com.umc.product.term.domain.enums.TermType;
import com.umc.product.term.domain.exception.TermDomainException;
import com.umc.product.term.domain.exception.TermErrorCode;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TermAgreementCommandService implements ManageTermAgreementUseCase {

    private final LoadTermPort loadTermPort;
    private final LoadTermConsentPort loadTermConsentPort;
    private final SaveTermConsentPort saveTermConsentPort;
    private final SaveTermConsentLogPort saveTermConsentLogPort;

    @Override
    public void createTermConsent(CreateTermConsentCommand command) {
        // 약관 존재 확인
        Term term = loadTermPort.findById(command.termId())
            .orElseThrow(() -> new TermDomainException(TermErrorCode.TERMS_NOT_FOUND));

        if (command.isAgreed()) {
            // 동의 처리
            // 이미 동의했는지 확인
            if (loadTermConsentPort.existsByMemberIdAndTermType(command.memberId(), term.getType())) {
                throw new TermDomainException(TermErrorCode.TERMS_CONSENT_ALREADY_EXISTS);
            }

            TermConsent termConsent = TermConsent.builder()
                .memberId(command.memberId())
                .termType(term.getType())
                .agreedAt(Instant.now())
                .build();

            saveTermConsentPort.save(termConsent);

            // 동의 로그 기록
            saveConsentLog(command.memberId(), term.getType(), TermConsentStatus.AGREED);
        } else {
            // 동의 철회 처리
            TermConsent termConsent = loadTermConsentPort
                .findByMemberIdAndTermType(command.memberId(), term.getType())
                .orElseThrow(() -> new TermDomainException(TermErrorCode.TERMS_CONSENT_NOT_FOUND));

            saveTermConsentPort.delete(termConsent);

            // 철회 로그 기록
            saveConsentLog(command.memberId(), term.getType(), TermConsentStatus.WITHDRAWN);
        }
    }

    private void saveConsentLog(Long memberId, TermType termType, TermConsentStatus status) {
        saveTermConsentLogPort.save(
            TermConsentLog.builder()
                .memberId(memberId)
                .termType(termType)
                .status(status)
                .build()
        );
    }
}
