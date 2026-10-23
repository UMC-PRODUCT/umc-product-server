package com.umc.product.term.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
        given(loadTermConsentPort.existsByMemberIdAndTermId(100L, 1L)).willReturn(false);

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
            .memberId(100L)
            .termId(1L)
            .isAgreed(true)
            .build();

        // when
        sut.createTermConsent(command);

        // then
        ArgumentCaptor<TermConsent> consentCaptor = ArgumentCaptor.forClass(TermConsent.class);
        then(saveTermConsentPort).should().save(consentCaptor.capture());
        then(saveTermConsentPort).should(never()).delete(any());

        ArgumentCaptor<TermConsentLog> logCaptor = ArgumentCaptor.forClass(TermConsentLog.class);
        then(saveTermConsentLogPort).should().save(logCaptor.capture());

        TermConsent savedConsent = consentCaptor.getValue();
        assertThat(savedConsent.getMemberId()).isEqualTo(100L);
        assertThat(savedConsent.getTermId()).isEqualTo(1L);
        assertThat(savedConsent.getTermType()).isEqualTo(TermType.SERVICE);

        TermConsentLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getMemberId()).isEqualTo(100L);
        assertThat(savedLog.getTermId()).isEqualTo(1L);
        assertThat(savedLog.getTermType()).isEqualTo(TermType.SERVICE);
        assertThat(savedLog.getStatus()).isEqualTo(TermConsentStatus.AGREED);
    }

    @Test
    void 이미_동의한_약관에_다시_동의하면_예외() {
        // given
        Term term = createTerms(TermType.SERVICE);
        given(loadTermPort.findById(1L)).willReturn(Optional.of(term));
        given(loadTermConsentPort.existsByMemberIdAndTermId(100L, 1L)).willReturn(true);

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
            .memberId(100L)
            .termId(1L)
            .isAgreed(true)
            .build();

        // when & then
        assertThatThrownBy(() -> sut.createTermConsent(command))
            .isInstanceOf(TermDomainException.class)
            .extracting("baseCode")
            .isEqualTo(TermErrorCode.TERMS_CONSENT_ALREADY_EXISTS);

        then(saveTermConsentPort).should(never()).save(any());
        then(saveTermConsentLogPort).should(never()).save(any());
    }


    @Test
    void 동의하지_않은_약관은_아무것도_저장하지_않는다() {
        // given
        Term term = createTerms(TermType.MARKETING);
        given(loadTermPort.findById(2L)).willReturn(Optional.of(term));

        CreateTermConsentCommand command = CreateTermConsentCommand.builder()
            .memberId(100L)
            .termId(2L)
            .isAgreed(false)
            .build();

        // when
        sut.createTermConsent(command);

        // then
        then(saveTermConsentPort).should(never()).save(any());
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
            .extracting("baseCode")
            .isEqualTo(TermErrorCode.TERMS_NOT_FOUND);

        then(saveTermConsentPort).should(never()).save(any());
        then(saveTermConsentLogPort).should(never()).save(any());
    }

    private Term createTerms(TermType type) {
        Term term = Term.builder()
            .type(type)
            .link("http://example.com/terms")
            .required(type == TermType.SERVICE || type == TermType.PRIVACY)
            .build();
        Long id = switch (type) {
            case SERVICE -> 1L;
            case MARKETING -> 2L;
            case PRIVACY -> 3L;
            case LOCATION -> 4L;
        };
        ReflectionTestUtils.setField(term, "id", id);
        return term;
    }
}
