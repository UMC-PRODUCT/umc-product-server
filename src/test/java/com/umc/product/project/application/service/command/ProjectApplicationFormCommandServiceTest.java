package com.umc.product.project.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.umc.product.project.application.port.in.command.dto.UpsertApplicationFormCommand;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPolicyPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationFormPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.survey.application.port.in.command.ManageFormUseCase;
import com.umc.product.survey.application.port.in.command.dto.CreateDraftFormCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateFormCommand;
import com.umc.product.survey.application.port.in.query.GetFormUseCase;
import com.umc.product.survey.application.port.in.query.dto.FormInfo;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo;
import com.umc.product.survey.domain.enums.FormStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationFormCommandServiceTest {

    @Mock
    LoadProjectPort loadProjectPort;
    @Mock
    LoadProjectApplicationFormPort loadApplicationFormPort;
    @Mock
    SaveProjectApplicationFormPort saveApplicationFormPort;
    @Mock
    LoadProjectApplicationFormPolicyPort loadPolicyPort;
    @Mock
    ManageFormUseCase manageFormUseCase;
    @Mock
    GetFormUseCase getFormUseCase;

    @InjectMocks
    ProjectApplicationFormCommandService sut;

    @Test
    void upsert_폼이_없으면_createDraft_호출_후_save() {
        // given
        Project project = createProject(42L, ProjectStatus.DRAFT, "Triple");
        given(loadProjectPort.getById(42L)).willReturn(project);
        given(loadApplicationFormPort.findByProjectId(42L)).willReturn(Optional.empty());

        given(manageFormUseCase.createDraft(any(CreateDraftFormCommand.class))).willReturn(500L);

        ProjectApplicationForm savedForm = createApplicationForm(project, 100L, 500L);
        given(saveApplicationFormPort.save(any(ProjectApplicationForm.class))).willReturn(savedForm);

        given(getFormUseCase.getFormWithStructure(500L)).willReturn(emptyFormStructure());
        given(loadPolicyPort.listByApplicationFormId(100L)).willReturn(List.of());

        UpsertApplicationFormCommand cmd = baseCommand(42L);

        // when
        ApplicationFormInfo result = sut.upsert(cmd);

        // then — createDraft 가 한 번 호출되고, getById/updateForm 은 호출되지 않음 (신규 생성이므로)
        ArgumentCaptor<CreateDraftFormCommand> createCaptor = ArgumentCaptor.forClass(CreateDraftFormCommand.class);
        then(manageFormUseCase).should().createDraft(createCaptor.capture());
        assertThat(createCaptor.getValue().title()).isEqualTo("Triple");
        assertThat(createCaptor.getValue().createdMemberId()).isEqualTo(99L);
        assertThat(createCaptor.getValue().isAnonymous()).isFalse();

        then(getFormUseCase).should(never()).getById(any());
        then(manageFormUseCase).should(never()).updateForm(any());

        assertThat(result.applicationFormId()).isEqualTo(100L);
        assertThat(result.projectId()).isEqualTo(42L);
    }

    @Test
    void upsert_본문_title이_null이고_Project_name도_null이면_default_title_사용() {
        // given
        Project project = createProject(42L, ProjectStatus.DRAFT, null);
        given(loadProjectPort.getById(42L)).willReturn(project);
        given(loadApplicationFormPort.findByProjectId(42L)).willReturn(Optional.empty());
        given(manageFormUseCase.createDraft(any())).willReturn(500L);
        given(saveApplicationFormPort.save(any())).willReturn(createApplicationForm(project, 100L, 500L));
        given(getFormUseCase.getFormWithStructure(500L)).willReturn(emptyFormStructure());
        given(loadPolicyPort.listByApplicationFormId(100L)).willReturn(List.of());

        UpsertApplicationFormCommand cmd = baseCommand(42L);

        // when
        sut.upsert(cmd);

        // then
        ArgumentCaptor<CreateDraftFormCommand> captor = ArgumentCaptor.forClass(CreateDraftFormCommand.class);
        then(manageFormUseCase).should().createDraft(captor.capture());
        assertThat(captor.getValue().title()).isEqualTo("프로젝트 지원서");
    }

    @Test
    void upsert_폼이_있고_meta가_같으면_updateForm_호출_안_함() {
        // given
        Project project = createProject(42L, ProjectStatus.DRAFT, "Triple");
        ProjectApplicationForm existing = createApplicationForm(project, 100L, 500L);

        given(loadProjectPort.getById(42L)).willReturn(project);
        given(loadApplicationFormPort.findByProjectId(42L)).willReturn(Optional.of(existing));
        given(getFormUseCase.getById(500L)).willReturn(formInfo(500L, "Triple", "팀 소개"));
        given(getFormUseCase.getFormWithStructure(500L)).willReturn(emptyFormStructure());
        given(loadPolicyPort.listByApplicationFormId(100L)).willReturn(List.of());

        UpsertApplicationFormCommand cmd = UpsertApplicationFormCommand.builder()
            .projectId(42L).requesterMemberId(99L)
            .title("Triple").description("팀 소개")
            .sections(List.of())
            .build();

        // when
        sut.upsert(cmd);

        // then
        then(manageFormUseCase).should(never()).createDraft(any());
        then(manageFormUseCase).should(never()).updateForm(any());
    }

    @Test
    void upsert_폼이_있고_title이_바뀌면_updateForm_호출() {
        // given
        Project project = createProject(42L, ProjectStatus.DRAFT, "Triple");
        ProjectApplicationForm existing = createApplicationForm(project, 100L, 500L);

        given(loadProjectPort.getById(42L)).willReturn(project);
        given(loadApplicationFormPort.findByProjectId(42L)).willReturn(Optional.of(existing));
        given(getFormUseCase.getById(500L)).willReturn(formInfo(500L, "예전 제목", null));
        given(getFormUseCase.getFormWithStructure(500L)).willReturn(emptyFormStructure());
        given(loadPolicyPort.listByApplicationFormId(100L)).willReturn(List.of());

        UpsertApplicationFormCommand cmd = UpsertApplicationFormCommand.builder()
            .projectId(42L).requesterMemberId(99L)
            .title("새 제목").description(null)
            .sections(List.of())
            .build();

        // when
        sut.upsert(cmd);

        // then
        ArgumentCaptor<UpdateFormCommand> captor = ArgumentCaptor.forClass(UpdateFormCommand.class);
        then(manageFormUseCase).should().updateForm(captor.capture());
        assertThat(captor.getValue().formId()).isEqualTo(500L);
        assertThat(captor.getValue().title()).isEqualTo("새 제목");
        assertThat(captor.getValue().description()).isNull();
    }

    @Test
    void upsert_폼이_있고_description이_바뀌면_updateForm_호출() {
        // given
        Project project = createProject(42L, ProjectStatus.DRAFT, "Triple");
        ProjectApplicationForm existing = createApplicationForm(project, 100L, 500L);

        given(loadProjectPort.getById(42L)).willReturn(project);
        given(loadApplicationFormPort.findByProjectId(42L)).willReturn(Optional.of(existing));
        given(getFormUseCase.getById(500L)).willReturn(formInfo(500L, "Triple", null));
        given(getFormUseCase.getFormWithStructure(500L)).willReturn(emptyFormStructure());
        given(loadPolicyPort.listByApplicationFormId(100L)).willReturn(List.of());

        UpsertApplicationFormCommand cmd = UpsertApplicationFormCommand.builder()
            .projectId(42L).requesterMemberId(99L)
            .title("Triple").description("새 설명")
            .sections(List.of())
            .build();

        // when
        sut.upsert(cmd);

        // then
        then(manageFormUseCase).should().updateForm(any(UpdateFormCommand.class));
    }

    @Test
    void upsert_Project가_COMPLETED면_PROJECT_INVALID_STATE() {
        Project project = createProject(42L, ProjectStatus.COMPLETED, "Triple");
        given(loadProjectPort.getById(42L)).willReturn(project);

        assertThatThrownBy(() -> sut.upsert(baseCommand(42L)))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);

        then(loadApplicationFormPort).should(never()).findByProjectId(any());
        then(manageFormUseCase).should(never()).createDraft(any());
    }

    @Test
    void upsert_Project가_ABORTED면_PROJECT_INVALID_STATE() {
        Project project = createProject(42L, ProjectStatus.ABORTED, "Triple");
        given(loadProjectPort.getById(42L)).willReturn(project);

        assertThatThrownBy(() -> sut.upsert(baseCommand(42L)))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);
    }

    @Test
    void upsert_Project가_IN_PROGRESS면_PROJECT_INVALID_STATE() {
        // Form 이 PUBLISHED 라 Survey 단에서도 차단되지만, Project 단에서 먼저 빠르게 실패한다.
        Project project = createProject(42L, ProjectStatus.IN_PROGRESS, "Triple");
        given(loadProjectPort.getById(42L)).willReturn(project);

        assertThatThrownBy(() -> sut.upsert(baseCommand(42L)))
            .isInstanceOf(ProjectDomainException.class)
            .extracting("baseCode")
            .isEqualTo(ProjectErrorCode.PROJECT_INVALID_STATE);

        // Survey 호출까지 가지 않음
        then(manageFormUseCase).should(never()).createDraft(any());
        then(manageFormUseCase).should(never()).updateForm(any());
    }

    private UpsertApplicationFormCommand baseCommand(Long projectId) {
        return UpsertApplicationFormCommand.builder()
            .projectId(projectId)
            .requesterMemberId(99L)
            .title(null)
            .description(null)
            .sections(List.of())
            .build();
    }

    private FormInfo formInfo(Long id, String title, String description) {
        return FormInfo.builder()
            .id(id)
            .createdMemberId(99L)
            .title(title)
            .description(description)
            .status(FormStatus.DRAFT)
            .isAnonymous(false)
            .build();
    }

    private FormWithStructureInfo emptyFormStructure() {
        return FormWithStructureInfo.builder()
            .formId(500L)
            .title("Triple")
            .description(null)
            .status(FormStatus.DRAFT)
            .isAnonymous(false)
            .sections(List.of())
            .build();
    }

    private Project createProject(Long id, ProjectStatus status, String name) {
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
        ReflectionTestUtils.setField(project, "status", status);
        ReflectionTestUtils.setField(project, "name", name);
        ReflectionTestUtils.setField(project, "productOwnerMemberId", 99L);
        return project;
    }

    private ProjectApplicationForm createApplicationForm(Project project, Long id, Long formId) {
        ProjectApplicationForm form = ProjectApplicationForm.create(project, formId);
        ReflectionTestUtils.setField(form, "id", id);
        return form;
    }
}
