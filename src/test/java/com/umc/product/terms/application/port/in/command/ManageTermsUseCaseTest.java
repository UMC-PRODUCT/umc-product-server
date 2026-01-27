package com.umc.product.terms.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.umc.product.terms.application.port.in.command.dto.CreateTermCommand;
import com.umc.product.terms.application.port.out.LoadTermsPort;
import com.umc.product.terms.application.port.out.SaveTermsPort;
import com.umc.product.terms.application.service.command.TermsCommandService;
import com.umc.product.terms.domain.Terms;
import com.umc.product.terms.domain.enums.TermsType;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ManageTermsUseCaseTest {

    @Mock
    SaveTermsPort saveTermsPort;

    @Mock
    LoadTermsPort loadTermsPort;

    @InjectMocks
    TermsCommandService sut;

    @Test
    void 필수_약관을_생성한다() {
        // given
        CreateTermCommand command = CreateTermCommand.builder()
                .type(TermsType.SERVICE)
                .title("서비스 이용약관")
                .content("서비스 이용약관 내용입니다.")
                .version("1.0")
                .required(true)
                .effectiveDate(Instant.now())
                .build();

        given(loadTermsPort.findActiveByType(TermsType.SERVICE)).willReturn(Optional.empty());
        given(saveTermsPort.save(any(Terms.class))).willAnswer(invocation -> {
            Terms terms = invocation.getArgument(0);
            ReflectionTestUtils.setField(terms, "id", 1L);
            return terms;
        });

        // when
        Long termsId = sut.createTerms(command);

        // then
        assertThat(termsId).isEqualTo(1L);
        then(saveTermsPort).should().save(any(Terms.class));
    }

    @Test
    void 선택_약관을_생성한다() {
        // given
        CreateTermCommand command = CreateTermCommand.builder()
                .type(TermsType.MARKETING)
                .title("마케팅 정보 수신 동의")
                .content("마케팅 정보 수신 동의 내용입니다.")
                .version("1.0")
                .required(false)
                .effectiveDate(Instant.now())
                .build();

        given(loadTermsPort.findActiveByType(TermsType.MARKETING)).willReturn(Optional.empty());
        given(saveTermsPort.save(any(Terms.class))).willAnswer(invocation -> {
            Terms terms = invocation.getArgument(0);
            ReflectionTestUtils.setField(terms, "id", 2L);
            return terms;
        });

        // when
        Long termsId = sut.createTerms(command);

        // then
        assertThat(termsId).isEqualTo(2L);
    }

}
