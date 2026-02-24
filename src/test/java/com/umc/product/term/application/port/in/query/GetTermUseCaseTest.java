package com.umc.product.term.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.umc.product.term.application.port.in.query.dto.TermInfo;
import com.umc.product.term.application.port.out.LoadTermPort;
import com.umc.product.term.application.service.query.TermQueryService;
import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.enums.TermType;
import com.umc.product.term.domain.exception.TermDomainException;
import com.umc.product.term.domain.exception.TermErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTermUseCaseTest {

    @Mock
    LoadTermPort loadTermPort;

    @InjectMocks
    TermQueryService sut;

    @Test
    void 타입으로_활성_약관을_조회한다() {
        // given
        Term term = createTerms(TermType.SERVICE, "서비스 이용약관", true);
        given(loadTermPort.findActiveByType(TermType.SERVICE)).willReturn(Optional.of(term));

        // when
        TermInfo result = sut.getTermsByType(TermType.SERVICE);

        // then
//        assertThat(result.title()).isEqualTo("서비스 이용약관");
        assertThat(result.type()).isEqualTo(TermType.SERVICE);
        assertThat(result.isMandatory()).isTrue();
    }

    @Test
    void 타입이_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> sut.getTermsByType(null))
            .isInstanceOf(TermDomainException.class)
            .extracting("code")
            .isEqualTo(TermErrorCode.TERMS_TYPE_REQUIRED);
    }

    @Test
    void 활성_약관이_없으면_예외() {
        // given
        given(loadTermPort.findActiveByType(TermType.SERVICE)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.getTermsByType(TermType.SERVICE))
            .isInstanceOf(TermDomainException.class)
            .extracting("code")
            .isEqualTo(TermErrorCode.TERMS_NOT_FOUND);
    }

    @Test
    void ID로_약관을_조회한다() {
        // given
        Term term = createTerms(TermType.PRIVACY, "개인정보 처리방침", true);
        given(loadTermPort.findById(1L)).willReturn(Optional.of(term));

        // when
        TermInfo result = sut.getTermsById(1L);

        // then
//        assertThat(result.title()).isEqualTo("개인정보 처리방침");
        assertThat(result.type()).isEqualTo(TermType.PRIVACY);
    }

    @Test
    void ID가_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> sut.getTermsById(null))
            .isInstanceOf(TermDomainException.class)
            .extracting("code")
            .isEqualTo(TermErrorCode.TERM_ID_REQUIRED);
    }

    @Test
    void 존재하지_않는_ID로_조회하면_예외() {
        // given
        given(loadTermPort.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.getTermsById(999L))
            .isInstanceOf(TermDomainException.class)
            .extracting("code")
            .isEqualTo(TermErrorCode.TERMS_NOT_FOUND);
    }

    private Term createTerms(TermType type, String title, boolean required) {
        return Term.builder()
            .type(type)
            .link("http://example.com/terms/" + type.name().toLowerCase())
            .required(required)
            .build();
    }
}
