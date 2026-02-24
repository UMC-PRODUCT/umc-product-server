package com.umc.product.term.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.term.application.port.in.command.dto.CreateTermConsentCommand;
import com.umc.product.term.application.port.out.LoadTermConsentPort;
import com.umc.product.term.application.port.out.LoadTermPort;
import com.umc.product.term.application.port.out.SaveTermConsentLogPort;
import com.umc.product.term.application.port.out.SaveTermConsentPort;
import com.umc.product.term.application.service.command.TermAgreementCommandService;
import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.TermConsent;
import com.umc.product.term.domain.TermConsentLog;
import com.umc.product.term.domain.enums.TermConsentStatus;
import com.umc.product.term.domain.enums.TermType;
import com.umc.product.term.domain.exception.TermDomainException;
import com.umc.product.term.domain.exception.TermErrorCode;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManageTermAgreementUseCaseTest {

    @Mock
    LoadTermPort loadTermPort;

    @Mock
    LoadTermConsentPort loadTermConsentPort;

    @Mock
    SaveTermConsentPort saveTermConsentPort;

    @Mock
    SaveTermConsentLogPort saveTermConsentLogPort;

    @InjectMocks
    TermAgreementCommandService sut;

    // ===== 동의 처리 =====

    @Test
    void 약관에_동의한다() {
        // given
        Term term = createTerms(TermType.SERVICE);
        given(loadTermPort.findById(1L)).willReturn(Optional.of(term));
        given(loadTermConsentPort.existsByMemberIdAndTermType(100L, TermType.SERVICE)).willReturn(false);

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
            .memberId(100L)
            .termId(1L)
            .isAgreed(true)
            .build();

        // when
        sut.createTermConsent(command);

        // then
        then(saveTermConsentPort).should().save(any(TermConsent.class));
        then(saveTermConsentPort).should(never()).delete(any());

        ArgumentCaptor<TermConsentLog> logCaptor = ArgumentCaptor.forClass(TermConsentLog.class);
        then(saveTermConsentLogPort).should().save(logCaptor.capture());

        TermConsentLog savedLog = logCaptor.getValue();
        assert savedLog.getMemberId().equals(100L);
        assert savedLog.getTermType() == TermType.SERVICE;
        assert savedLog.getStatus() == TermConsentStatus.AGREED;
    }

    @Test
    void 이미_동의한_약관에_다시_동의하면_예외() {
        // given
        Term term = createTerms(TermType.SERVICE);
        given(loadTermPort.findById(1L)).willReturn(Optional.of(term));
        given(loadTermConsentPort.existsByMemberIdAndTermType(100L, TermType.SERVICE)).willReturn(true);

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
            .memberId(100L)
            .termId(1L)
            .isAgreed(true)
            .build();

        // when & then
        assertThatThrownBy(() -> sut.createTermConsent(command))
            .isInstanceOf(TermDomainException.class)
            .extracting("code")
            .isEqualTo(TermErrorCode.TERMS_CONSENT_ALREADY_EXISTS);

        then(saveTermConsentPort).should(never()).save(any());
        then(saveTermConsentLogPort).should(never()).save(any());
    }

    // ===== 철회 처리 =====

    @Test
    void 약관_동의를_철회한다() {
        // given
        Term term = createTerms(TermType.MARKETING);
        TermConsent existingConsent = TermConsent.builder()
            .memberId(100L)
            .termType(TermType.MARKETING)
            .agreedAt(Instant.now())
            .build();

        given(loadTermPort.findById(2L)).willReturn(Optional.of(term));
        given(loadTermConsentPort.findByMemberIdAndTermType(100L, TermType.MARKETING))
            .willReturn(Optional.of(existingConsent));

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
            .memberId(100L)
            .termId(2L)
            .isAgreed(false)
            .build();

        // when
        sut.createTermConsent(command);

        // then
        then(saveTermConsentPort).should().delete(existingConsent);
        then(saveTermConsentPort).should(never()).save(any());

        ArgumentCaptor<TermConsentLog> logCaptor = ArgumentCaptor.forClass(TermConsentLog.class);
        then(saveTermConsentLogPort).should().save(logCaptor.capture());

        TermConsentLog savedLog = logCaptor.getValue();
        assert savedLog.getMemberId().equals(100L);
        assert savedLog.getTermType() == TermType.MARKETING;
        assert savedLog.getStatus() == TermConsentStatus.WITHDRAWN;
    }

    @Test
    void 동의하지_않은_약관을_철회하면_예외() {
        // given
        Term term = createTerms(TermType.MARKETING);
        given(loadTermPort.findById(2L)).willReturn(Optional.of(term));
        given(loadTermConsentPort.findByMemberIdAndTermType(100L, TermType.MARKETING))
            .willReturn(Optional.empty());

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
            .memberId(100L)
            .termId(2L)
            .isAgreed(false)
            .build();

        // when & then
        assertThatThrownBy(() -> sut.createTermConsent(command))
            .isInstanceOf(TermDomainException.class)
            .extracting("code")
            .isEqualTo(TermErrorCode.TERMS_CONSENT_NOT_FOUND);

        then(saveTermConsentPort).should(never()).delete(any());
        then(saveTermConsentLogPort).should(never()).save(any());
    }

    // ===== 공통 예외 =====

    @Test
    void 존재하지_않는_약관에_동의하면_예외() {
        // given
        given(loadTermPort.findById(999L)).willReturn(Optional.empty());

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
            .memberId(100L)
            .termId(999L)
            .isAgreed(true)
            .build();

        // when & then
        assertThatThrownBy(() -> sut.createTermConsent(command))
            .isInstanceOf(TermDomainException.class)
            .extracting("code")
            .isEqualTo(TermErrorCode.TERMS_NOT_FOUND);

        then(saveTermConsentPort).should(never()).save(any());
        then(saveTermConsentLogPort).should(never()).save(any());
    }

    private Term createTerms(TermType type) {
        return Term.builder()
            .type(type)
            .link("http://example.com/terms")
            .required(type == TermType.SERVICE || type == TermType.PRIVACY)
            .build();
    }
}
