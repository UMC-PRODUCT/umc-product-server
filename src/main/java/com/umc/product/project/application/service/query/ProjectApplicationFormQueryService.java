package com.umc.product.project.application.service.query;

import com.umc.product.project.application.port.in.query.GetProjectApplicationFormUseCase;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPolicyPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import com.umc.product.survey.application.port.in.query.GetFormUseCase;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 지원 폼 조회 서비스 (PROJECT-106-GET).
 * <p>
 * 폼 메타와 섹션→질문→옵션 nested 구조는 Survey 도메인에 위임하며, Project 도메인의 정책({@link ProjectApplicationFormPolicy})
 * 을 합성해 단일 응답을 만든다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectApplicationFormQueryService implements GetProjectApplicationFormUseCase {

    private final LoadProjectApplicationFormPort loadApplicationFormPort;
    private final LoadProjectApplicationFormPolicyPort loadPolicyPort;

    // Cross-domain
    private final GetFormUseCase getFormUseCase;

    @Override
    public Optional<ApplicationFormInfo> findByProjectId(Long projectId) {
        return loadApplicationFormPort.findByProjectId(projectId)
            .map(this::assemble);
    }

    private ApplicationFormInfo assemble(ProjectApplicationForm applicationForm) {
        FormWithStructureInfo formStructure = getFormUseCase.getFormWithStructure(applicationForm.getFormId());
        List<ProjectApplicationFormPolicy> policies = loadPolicyPort.listByApplicationFormId(applicationForm.getId());
        return ApplicationFormInfo.of(applicationForm, formStructure, policies);
    }
}
