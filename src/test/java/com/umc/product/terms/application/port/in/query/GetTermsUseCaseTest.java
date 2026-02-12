package com.umc.product.terms.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.umc.product.terms.application.port.in.query.dto.TermsInfo;
import com.umc.product.terms.application.port.out.LoadTermsPort;
import com.umc.product.terms.application.service.query.TermsQueryService;
import com.umc.product.terms.domain.Terms;
import com.umc.product.terms.domain.enums.TermsType;
import com.umc.product.terms.domain.exception.TermsDomainException;
import com.umc.product.terms.domain.exception.TermsErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTermsUseCaseTest {

    @Mock
    LoadTermsPort loadTermsPort;

    @InjectMocks
    TermsQueryService sut;

    @Test
    void 타입으로_활성_약관을_조회한다() {
        // given
        Terms terms = createTerms(TermsType.SERVICE, "서비스 이용약관", true);
        given(loadTermsPort.findActiveByType(TermsType.SERVICE)).willReturn(Optional.of(terms));

        // when
        TermsInfo result = sut.getTermsByType(TermsType.SERVICE);

        // then
//        assertThat(result.title()).isEqualTo("서비스 이용약관");
        assertThat(result.type()).isEqualTo(TermsType.SERVICE);
        assertThat(result.isMandatory()).isTrue();
    }

    @Test
    void 타입이_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> sut.getTermsByType(null))
            .isInstanceOf(TermsDomainException.class)
            .extracting("code")
            .isEqualTo(TermsErrorCode.TERMS_TYPE_REQUIRED);
    }

    @Test
    void 활성_약관이_없으면_예외() {
        // given
        given(loadTermsPort.findActiveByType(TermsType.SERVICE)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.getTermsByType(TermsType.SERVICE))
            .isInstanceOf(TermsDomainException.class)
            .extracting("code")
            .isEqualTo(TermsErrorCode.TERMS_NOT_FOUND);
    }

    @Test
    void ID로_약관을_조회한다() {
        // given
        Terms terms = createTerms(TermsType.PRIVACY, "개인정보 처리방침", true);
        given(loadTermsPort.findById(1L)).willReturn(Optional.of(terms));

        // when
        TermsInfo result = sut.getTermsById(1L);

        // then
//        assertThat(result.title()).isEqualTo("개인정보 처리방침");
        assertThat(result.type()).isEqualTo(TermsType.PRIVACY);
    }

    @Test
    void ID가_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> sut.getTermsById(null))
            .isInstanceOf(TermsDomainException.class)
            .extracting("code")
            .isEqualTo(TermsErrorCode.TERM_ID_REQUIRED);
    }

    @Test
    void 존재하지_않는_ID로_조회하면_예외() {
        // given
        given(loadTermsPort.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.getTermsById(999L))
            .isInstanceOf(TermsDomainException.class)
            .extracting("code")
            .isEqualTo(TermsErrorCode.TERMS_NOT_FOUND);
    }

    private Terms createTerms(TermsType type, String title, boolean required) {
        return Terms.builder()
            .type(type)
            .link("http://example.com/terms/" + type.name().toLowerCase())
            .required(required)
            .build();
    }
}
