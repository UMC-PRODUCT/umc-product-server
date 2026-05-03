package com.umc.product.project.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPolicyPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import com.umc.product.project.domain.enums.FormSectionType;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.survey.application.port.in.query.GetFormUseCase;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo;
import com.umc.product.survey.domain.enums.FormStatus;
import com.umc.product.survey.domain.enums.QuestionType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationFormQueryServiceTest {

    @Mock
    LoadProjectApplicationFormPort loadApplicationFormPort;
    @Mock
    LoadProjectApplicationFormPolicyPort loadPolicyPort;
    @Mock
    GetFormUseCase getFormUseCase;

    @InjectMocks
    ProjectApplicationFormQueryService sut;

    @Test
    void findByProjectId_폼이_없으면_empty_반환() {
        given(loadApplicationFormPort.findByProjectId(42L)).willReturn(Optional.empty());

        Optional<ApplicationFormInfo> result = sut.findByProjectId(42L);

        assertThat(result).isEmpty();
        then(getFormUseCase).should(never()).getFormWithStructure(any());
        then(loadPolicyPort).should(never()).listByApplicationFormId(any());
    }

    @Test
    void findByProjectId_폼이_있으면_FormWithStructure와_정책을_합성() {
        // given
        Project project = createProject(42L);
        ProjectApplicationForm applicationForm = createApplicationForm(project, 100L, 500L);

        given(loadApplicationFormPort.findByProjectId(42L)).willReturn(Optional.of(applicationForm));
        given(getFormUseCase.getFormWithStructure(500L)).willReturn(buildFormStructure());
        given(loadPolicyPort.listByApplicationFormId(100L)).willReturn(List.of(
            ProjectApplicationFormPolicy.createCommon(applicationForm, 1000L),
            ProjectApplicationFormPolicy.createForParts(applicationForm, 1001L,
                Set.of(ChallengerPart.WEB, ChallengerPart.IOS))
        ));

        // when
        ApplicationFormInfo result = sut.findByProjectId(42L).orElseThrow();

        // then
        assertThat(result.projectId()).isEqualTo(42L);
        assertThat(result.applicationFormId()).isEqualTo(100L);
        assertThat(result.title()).isEqualTo("Triple 지원서");
        assertThat(result.sections()).hasSize(2);

        var commonSection = result.sections().get(0);
        assertThat(commonSection.sectionId()).isEqualTo(1000L);
        assertThat(commonSection.type()).isEqualTo(FormSectionType.COMMON);
        assertThat(commonSection.allowedParts()).isEmpty();
        assertThat(commonSection.questions()).hasSize(1);

        var partSection = result.sections().get(1);
        assertThat(partSection.sectionId()).isEqualTo(1001L);
        assertThat(partSection.type()).isEqualTo(FormSectionType.PART);
        assertThat(partSection.allowedParts())
            .containsExactlyInAnyOrder(ChallengerPart.WEB, ChallengerPart.IOS);
        assertThat(partSection.questions()).hasSize(1);
        assertThat(partSection.questions().get(0).options()).hasSize(2);
    }

    @Test
    void findByProjectId_정책이_누락된_섹션은_PART와_빈_parts로_폴백() {
        // given — Survey 단엔 섹션 2개 있지만 Project 정책은 1개만 (데이터 정합 깨진 시나리오)
        Project project = createProject(42L);
        ProjectApplicationForm applicationForm = createApplicationForm(project, 100L, 500L);

        given(loadApplicationFormPort.findByProjectId(42L)).willReturn(Optional.of(applicationForm));
        given(getFormUseCase.getFormWithStructure(500L)).willReturn(buildFormStructure());
        given(loadPolicyPort.listByApplicationFormId(100L)).willReturn(List.of(
            ProjectApplicationFormPolicy.createCommon(applicationForm, 1000L)
            // 1001L 정책 누락
        ));

        // when
        ApplicationFormInfo result = sut.findByProjectId(42L).orElseThrow();

        // then
        var orphanSection = result.sections().get(1);
        assertThat(orphanSection.type()).isEqualTo(FormSectionType.PART);
        assertThat(orphanSection.allowedParts()).isEmpty();
    }

    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }

    private Project createProject(Long id) {
        Project project;
        try {
            var constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            project = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ReflectionTestUtils.setField(project, "id", id);
        ReflectionTestUtils.setField(project, "gisuId", 1L);
        ReflectionTestUtils.setField(project, "chapterId", 1L);
        ReflectionTestUtils.setField(project, "status", ProjectStatus.DRAFT);
        ReflectionTestUtils.setField(project, "name", "Triple");
        ReflectionTestUtils.setField(project, "productOwnerMemberId", 10L);
        ReflectionTestUtils.setField(project, "createdByMemberId", 10L);
        return project;
    }

    private ProjectApplicationForm createApplicationForm(Project project, Long id, Long formId) {
        ProjectApplicationForm form = ProjectApplicationForm.create(project, formId);
        ReflectionTestUtils.setField(form, "id", id);
        return form;
    }

    private FormWithStructureInfo buildFormStructure() {
        return FormWithStructureInfo.builder()
            .formId(500L)
            .title("Triple 지원서")
            .description(null)
            .status(FormStatus.DRAFT)
            .isAnonymous(false)
            .sections(List.of(
                FormWithStructureInfo.SectionWithQuestions.builder()
                    .sectionId(1000L)
                    .title("공통 문항")
                    .description(null)
                    .orderNo(1L)
                    .questions(List.of(
                        FormWithStructureInfo.QuestionWithOptions.builder()
                            .questionId(2000L)
                            .title("자기소개")
                            .description(null)
                            .type(QuestionType.LONG_TEXT)
                            .isRequired(true)
                            .orderNo(1L)
                            .options(List.of())
                            .build()
                    ))
                    .build(),
                FormWithStructureInfo.SectionWithQuestions.builder()
                    .sectionId(1001L)
                    .title("프론트엔드")
                    .description(null)
                    .orderNo(2L)
                    .questions(List.of(
                        FormWithStructureInfo.QuestionWithOptions.builder()
                            .questionId(2001L)
                            .title("선호 프레임워크")
                            .description(null)
                            .type(QuestionType.RADIO)
                            .isRequired(true)
                            .orderNo(1L)
                            .options(List.of(
                                FormWithStructureInfo.Option.builder()
                                    .optionId(3000L)
                                    .content("React")
                                    .orderNo(1L)
                                    .isOther(false)
                                    .build(),
                                FormWithStructureInfo.Option.builder()
                                    .optionId(3001L)
                                    .content("Vue")
                                    .orderNo(2L)
                                    .isOther(false)
                                    .build()
                            ))
                            .build()
                    ))
                    .build()
            ))
            .build();
    }
}
