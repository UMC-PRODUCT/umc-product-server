package com.umc.product.term.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.umc.product.term.application.port.in.command.dto.CreateTermCommand;
import com.umc.product.term.application.port.out.LoadTermPort;
import com.umc.product.term.application.port.out.SaveTermPort;
import com.umc.product.term.application.service.command.TermCommandService;
import com.umc.product.term.domain.Term;
import com.umc.product.term.domain.enums.TermType;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ManageTermUseCaseTest {

    @Mock
    SaveTermPort saveTermPort;

    @Mock
    LoadTermPort loadTermPort;

    @InjectMocks
    TermCommandService sut;

    @Test
    void 필수_약관을_생성한다() {
        // given
        CreateTermCommand command = CreateTermCommand.builder()
            .type(TermType.SERVICE)
            .link("https://example.com/terms/service")
            .required(true)
            .build();

        given(loadTermPort.findActiveByType(TermType.SERVICE)).willReturn(Optional.empty());
        given(saveTermPort.save(any(Term.class))).willAnswer(invocation -> {
            Term term = invocation.getArgument(0);
            ReflectionTestUtils.setField(term, "id", 1L);
            return term;
        });

        // when
        Long termsId = sut.createTerms(command);

        // then
        assertThat(termsId).isEqualTo(1L);
        then(saveTermPort).should().save(any(Term.class));
    }

    @Test
    void 선택_약관을_생성한다() {
        // given
        CreateTermCommand command = CreateTermCommand.builder()
            .type(TermType.MARKETING)
            .link("https://example.com/terms/marketing")
            .required(false)
            .build();

        given(loadTermPort.findActiveByType(TermType.MARKETING)).willReturn(Optional.empty());
        given(saveTermPort.save(any(Term.class))).willAnswer(invocation -> {
            Term term = invocation.getArgument(0);
            ReflectionTestUtils.setField(term, "id", 2L);
            return term;
        });

        // when
        Long termsId = sut.createTerms(command);

        // then
        assertThat(termsId).isEqualTo(2L);
    }
}
