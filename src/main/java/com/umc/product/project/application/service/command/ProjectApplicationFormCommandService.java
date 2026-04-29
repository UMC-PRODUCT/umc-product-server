package com.umc.product.project.application.service.command;

import com.umc.product.project.application.port.in.command.UpsertProjectApplicationFormUseCase;
import com.umc.product.project.application.port.in.command.dto.UpsertApplicationFormCommand;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPolicyPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.application.port.out.SaveProjectApplicationFormPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import com.umc.product.survey.application.port.in.command.ManageFormUseCase;
import com.umc.product.survey.application.port.in.command.dto.CreateDraftFormCommand;
import com.umc.product.survey.application.port.in.command.dto.UpdateFormCommand;
import com.umc.product.survey.application.port.in.query.GetFormUseCase;
import com.umc.product.survey.application.port.in.query.dto.FormInfo;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 지원 폼 upsert 서비스 (PROJECT-106).
 * <p>
 * 본 커밋(C6)은 폼 라이프사이클(부재 시 생성 / 메타 sync) 까지만 다룬다.
 * 섹션 / 질문 / 옵션 diff orchestration 은 후속 커밋(C7)에서 추가된다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectApplicationFormCommandService implements UpsertProjectApplicationFormUseCase {

    /**
     * 본문 title 도 null, Project.name 도 null 인 DRAFT 단계에서 사용할 기본 폼 제목.
     * Survey 단의 {@code Form.title} 이 NOT NULL 이라 어떤 값이든 채워야 한다.
     */
    private static final String DEFAULT_FORM_TITLE = "프로젝트 지원서";

    private final LoadProjectPort loadProjectPort;
    private final LoadProjectApplicationFormPort loadApplicationFormPort;
    private final SaveProjectApplicationFormPort saveApplicationFormPort;
    private final LoadProjectApplicationFormPolicyPort loadPolicyPort;

    // Cross-domain
    private final ManageFormUseCase manageFormUseCase;
    private final GetFormUseCase getFormUseCase;

    @Override
    public ApplicationFormInfo upsert(UpsertApplicationFormCommand command) {
        Project project = loadProjectPort.getById(command.projectId());
        project.validateApplicationFormEditable();

        ProjectApplicationForm applicationForm = loadApplicationFormPort.findByProjectId(command.projectId())
            .map(existing -> {
                syncFormMetaIfChanged(existing, project, command);
                return existing;
            })
            .orElseGet(() -> createApplicationForm(project, command));

        // TODO (C7): 섹션 / 질문 / 옵션 diff orchestration

        return assembleResponse(applicationForm);
    }

    private ProjectApplicationForm createApplicationForm(Project project, UpsertApplicationFormCommand command) {
        Long formId = manageFormUseCase.createDraft(
            CreateDraftFormCommand.builder()
                .createdMemberId(command.requesterMemberId())
                .title(resolveTitle(project, command))
                .description(command.description())
                .isAnonymous(false)
                .build()
        );
        return saveApplicationFormPort.save(ProjectApplicationForm.create(project, formId));
    }

    private void syncFormMetaIfChanged(
        ProjectApplicationForm applicationForm,
        Project project,
        UpsertApplicationFormCommand command
    ) {
        FormInfo existing = getFormUseCase.getById(applicationForm.getFormId());
        String resolvedTitle = resolveTitle(project, command);

        if (Objects.equals(existing.title(), resolvedTitle)
            && Objects.equals(existing.description(), command.description())) {
            return;
        }

        manageFormUseCase.updateForm(
            UpdateFormCommand.builder()
                .formId(applicationForm.getFormId())
                .requesterMemberId(command.requesterMemberId())
                .title(resolvedTitle)
                .description(command.description())
                .isAnonymous(existing.isAnonymous())
                .build()
        );
    }

    /**
     * 폼 제목 fallback: 본문 title → {@code Project.name} → "프로젝트 지원서".
     */
    private String resolveTitle(Project project, UpsertApplicationFormCommand command) {
        if (command.title() != null) {
            return command.title();
        }
        if (project.getName() != null) {
            return project.getName();
        }
        return DEFAULT_FORM_TITLE;
    }

    private ApplicationFormInfo assembleResponse(ProjectApplicationForm applicationForm) {
        FormWithStructureInfo formStructure = getFormUseCase.getFormWithStructure(applicationForm.getFormId());
        List<ProjectApplicationFormPolicy> policies =
            loadPolicyPort.listByApplicationFormId(applicationForm.getId());
        return ApplicationFormInfo.of(applicationForm, formStructure, policies);
    }
}
