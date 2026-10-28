package com.umc.product.project.application.service.policy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authorization.application.port.in.query.CheckChallengerAuthorityUseCase;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;

@ExtendWith(MockitoExtension.class)
class ProjectManualMatchingAccessPolicyTest {

    @Mock
    CheckChallengerAuthorityUseCase authorityUseCase;

    @InjectMocks
    ProjectManualMatchingAccessPolicy sut;

    @Test
    void 해당_기수_총괄단_권한이_있으면_지부_권한은_조회하지_않는다() {
        // given
        given(authorityUseCase.isCentralCoreInGisu(10L, 1L)).willReturn(true);

        // when & then
        assertThatCode(() -> sut.validateChapter(10L, 1L, 7L)).doesNotThrowAnyException();
        verify(authorityUseCase).isCentralCoreInGisu(10L, 1L);
        verifyNoMoreInteractions(authorityUseCase);
    }

    @Test
    void 지부_권한은_요청한_기수와_지부로_판정한다() {
        // given
        given(authorityUseCase.isChapterPresidentInGisu(10L, 1L, 7L)).willReturn(true);

        // when & then
        assertThatCode(() -> sut.validateChapter(10L, 1L, 7L)).doesNotThrowAnyException();
        assertThatThrownBy(() -> sut.validateChapter(10L, 1L, 8L))
            .isInstanceOf(ProjectDomainException.class);
        assertThatThrownBy(() -> sut.validateChapter(10L, 2L, 7L))
            .isInstanceOf(ProjectDomainException.class);
    }

    @Test
    void 총괄단과_지부장_권한이_모두_없으면_거부한다() {
        // when & then
        assertThatThrownBy(() -> sut.validateChapter(10L, 1L, 7L))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode").isEqualTo(ProjectErrorCode.PROJECT_ACCESS_DENIED);
    }

    @Test
    void 단건_프로젝트의_기수와_지부로_권한을_검증한다() {
        // given
        Project project = Project.createDraft(1L, 7L, 20L, 1L, 20L);
        given(authorityUseCase.isChapterPresidentInGisu(10L, 1L, 7L)).willReturn(true);

        // when & then
        assertThatCode(() -> sut.validate(10L, project)).doesNotThrowAnyException();
        verify(authorityUseCase).isChapterPresidentInGisu(10L, 1L, 7L);
    }

    @Test
    void 같은_기수와_지부의_프로젝트는_권한을_한_번만_검증한다() {
        // given
        Project first = Project.createDraft(1L, 7L, 20L, 1L, 20L);
        Project second = Project.createDraft(1L, 7L, 30L, 1L, 30L);
        given(authorityUseCase.isChapterPresidentInGisu(10L, 1L, 7L)).willReturn(true);

        // when
        sut.validateAll(10L, List.of(first, second));

        // then
        verify(authorityUseCase).isCentralCoreInGisu(10L, 1L);
        verify(authorityUseCase).isChapterPresidentInGisu(10L, 1L, 7L);
        verifyNoMoreInteractions(authorityUseCase);
    }
}
