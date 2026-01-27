package com.umc.product.terms.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.terms.application.port.in.command.dto.CreateTermConsentCommand;
import com.umc.product.terms.application.port.out.LoadTermsConsentPort;
import com.umc.product.terms.application.port.out.LoadTermsPort;
import com.umc.product.terms.application.port.out.SaveTermsConsentLogPort;
import com.umc.product.terms.application.port.out.SaveTermsConsentPort;
import com.umc.product.terms.application.service.command.TermsAgreementCommandService;
import com.umc.product.terms.domain.Terms;
import com.umc.product.terms.domain.TermsConsent;
import com.umc.product.terms.domain.TermsConsentLog;
import com.umc.product.terms.domain.enums.TermsConsentStatus;
import com.umc.product.terms.domain.enums.TermsType;
import com.umc.product.terms.domain.exception.TermsDomainException;
import com.umc.product.terms.domain.exception.TermsErrorCode;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManageTermsAgreementUseCaseTest {

    @Mock
    LoadTermsPort loadTermsPort;

    @Mock
    LoadTermsConsentPort loadTermsConsentPort;

    @Mock
    SaveTermsConsentPort saveTermsConsentPort;

    @Mock
    SaveTermsConsentLogPort saveTermsConsentLogPort;

    @InjectMocks
    TermsAgreementCommandService sut;

    // ===== 동의 처리 =====

    @Test
    void 약관에_동의한다() {
        // given
        Terms terms = createTerms(TermsType.SERVICE);
        given(loadTermsPort.findById(1L)).willReturn(Optional.of(terms));
        given(loadTermsConsentPort.existsByMemberIdAndTermType(100L, TermsType.SERVICE)).willReturn(false);

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
                .memberId(100L)
                .termId(1L)
                .isAgreed(true)
                .build();

        // when
        sut.createTermConsent(command);

        // then
        then(saveTermsConsentPort).should().save(any(TermsConsent.class));
        then(saveTermsConsentPort).should(never()).delete(any());

        ArgumentCaptor<TermsConsentLog> logCaptor = ArgumentCaptor.forClass(TermsConsentLog.class);
        then(saveTermsConsentLogPort).should().save(logCaptor.capture());

        TermsConsentLog savedLog = logCaptor.getValue();
        assert savedLog.getMemberId().equals(100L);
        assert savedLog.getTermType() == TermsType.SERVICE;
        assert savedLog.getStatus() == TermsConsentStatus.AGREED;
    }

    @Test
    void 이미_동의한_약관에_다시_동의하면_예외() {
        // given
        Terms terms = createTerms(TermsType.SERVICE);
        given(loadTermsPort.findById(1L)).willReturn(Optional.of(terms));
        given(loadTermsConsentPort.existsByMemberIdAndTermType(100L, TermsType.SERVICE)).willReturn(true);

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
                .memberId(100L)
                .termId(1L)
                .isAgreed(true)
                .build();

        // when & then
        assertThatThrownBy(() -> sut.createTermConsent(command))
                .isInstanceOf(TermsDomainException.class)
                .extracting("code")
                .isEqualTo(TermsErrorCode.TERMS_CONSENT_ALREADY_EXISTS);

        then(saveTermsConsentPort).should(never()).save(any());
        then(saveTermsConsentLogPort).should(never()).save(any());
    }

    // ===== 철회 처리 =====

    @Test
    void 약관_동의를_철회한다() {
        // given
        Terms terms = createTerms(TermsType.MARKETING);
        TermsConsent existingConsent = TermsConsent.builder()
                .memberId(100L)
                .termType(TermsType.MARKETING)
                .agreedAt(Instant.now())
                .build();

        given(loadTermsPort.findById(2L)).willReturn(Optional.of(terms));
        given(loadTermsConsentPort.findByMemberIdAndTermType(100L, TermsType.MARKETING))
                .willReturn(Optional.of(existingConsent));

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
                .memberId(100L)
                .termId(2L)
                .isAgreed(false)
                .build();

        // when
        sut.createTermConsent(command);

        // then
        then(saveTermsConsentPort).should().delete(existingConsent);
        then(saveTermsConsentPort).should(never()).save(any());

        ArgumentCaptor<TermsConsentLog> logCaptor = ArgumentCaptor.forClass(TermsConsentLog.class);
        then(saveTermsConsentLogPort).should().save(logCaptor.capture());

        TermsConsentLog savedLog = logCaptor.getValue();
        assert savedLog.getMemberId().equals(100L);
        assert savedLog.getTermType() == TermsType.MARKETING;
        assert savedLog.getStatus() == TermsConsentStatus.WITHDRAWN;
    }

    @Test
    void 동의하지_않은_약관을_철회하면_예외() {
        // given
        Terms terms = createTerms(TermsType.MARKETING);
        given(loadTermsPort.findById(2L)).willReturn(Optional.of(terms));
        given(loadTermsConsentPort.findByMemberIdAndTermType(100L, TermsType.MARKETING))
                .willReturn(Optional.empty());

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
                .memberId(100L)
                .termId(2L)
                .isAgreed(false)
                .build();

        // when & then
        assertThatThrownBy(() -> sut.createTermConsent(command))
                .isInstanceOf(TermsDomainException.class)
                .extracting("code")
                .isEqualTo(TermsErrorCode.TERMS_CONSENT_NOT_FOUND);

        then(saveTermsConsentPort).should(never()).delete(any());
        then(saveTermsConsentLogPort).should(never()).save(any());
    }

    // ===== 공통 예외 =====

    @Test
    void 존재하지_않는_약관에_동의하면_예외() {
        // given
        given(loadTermsPort.findById(999L)).willReturn(Optional.empty());

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
                .memberId(100L)
                .termId(999L)
                .isAgreed(true)
                .build();

        // when & then
        assertThatThrownBy(() -> sut.createTermConsent(command))
                .isInstanceOf(TermsDomainException.class)
                .extracting("code")
                .isEqualTo(TermsErrorCode.TERMS_NOT_FOUND);

        then(saveTermsConsentPort).should(never()).save(any());
        then(saveTermsConsentLogPort).should(never()).save(any());
    }

    private Terms createTerms(TermsType type) {
        return Terms.builder()
                .type(type)
                .title(type.name() + " 약관")
                .content(type.name() + " 약관 내용")
                .version("1.0")
                .required(type == TermsType.SERVICE || type == TermsType.PRIVACY)
                .effectiveDate(Instant.now())
                .build();
    }
}
