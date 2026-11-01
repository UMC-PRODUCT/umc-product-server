package com.umc.product.term.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.term.application.port.in.query.dto.TermInfo;
import com.umc.product.term.application.port.out.LoadTermConsentPort;
import com.umc.product.term.application.port.out.LoadTermPort;
import com.umc.product.term.application.service.query.TermAgreementQueryService;
import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.TermConsent;
import com.umc.product.term.domain.enums.TermType;

@ExtendWith(MockitoExtension.class)
class TermAgreementQueryServiceTest {

    @Mock
    LoadTermConsentPort loadTermConsentPort;

    @Mock
    LoadTermPort loadTermPort;

    @InjectMocks
    TermAgreementQueryService sut;

    @Test
    @DisplayName("동의한 약관은 현재 활성 약관이 아니라 저장된 약관 ID 기준으로 조회한다")
    void 동의한_약관은_저장된_약관_ID_기준으로_조회한다() {
        // given
        Term oldServiceTerm = createTerm(10L, TermType.SERVICE, "https://example.com/terms/service-v1");
        Term privacyTerm = createTerm(20L, TermType.PRIVACY, "https://example.com/terms/privacy-v1");

        given(loadTermConsentPort.findByMemberId(100L)).willReturn(List.of(
            createConsent(100L, oldServiceTerm),
            createConsent(100L, privacyTerm)
        ));
        given(loadTermPort.listByIds(List.of(10L, 20L))).willReturn(List.of(oldServiceTerm, privacyTerm));

        // when
        List<TermInfo> result = sut.getAgreedTermsByMemberId(100L);

        // then
        assertThat(result)
            .extracting(TermInfo::id)
            .containsExactly(10L, 20L);
    }

    private TermConsent createConsent(Long memberId, Term term) {
        return TermConsent.builder()
            .memberId(memberId)
            .termId(term.getId())
            .termType(term.getType())
            .agreedAt(Instant.parse("2026-05-26T00:00:00Z"))
            .build();
    }

    private Term createTerm(Long id, TermType type, String link) {
        Term term = Term.builder()
            .type(type)
            .link(link)
            .required(true)
            .build();
        ReflectionTestUtils.setField(term, "id", id);
        return term;
    }
}
