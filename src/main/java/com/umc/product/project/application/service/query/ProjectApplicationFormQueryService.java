package com.umc.product.project.application.service.query;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.in.query.GetProjectApplicationFormUseCase;
import com.umc.product.project.application.port.in.query.dto.ApplicationFormInfo;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPolicyPort;
import com.umc.product.project.application.port.out.LoadProjectApplicationFormPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.ProjectApplicationForm;
import com.umc.product.project.domain.ProjectApplicationFormPolicy;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import com.umc.product.survey.application.port.in.query.GetFormUseCase;
import com.umc.product.survey.application.port.in.query.dto.FormWithStructureInfo;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 지원 폼 조회 서비스 (PROJECT-106-GET).
 * <p>
 * 폼 메타와 섹션→질문→옵션 nested 구조는 Survey 도메인에 위임하며, Project 도메인의 정책({@link ProjectApplicationFormPolicy}) 을 합성해 단일 응답을 만든다.
 * 호출자 역할에 따라 전체/마스킹된 섹션을 차등 노출한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectApplicationFormQueryService implements GetProjectApplicationFormUseCase {

    private final LoadProjectApplicationFormPort loadApplicationFormPort;
    private final LoadProjectApplicationFormPolicyPort loadPolicyPort;

    // Cross-domain
    private final GetFormUseCase getFormUseCase;
    private final GetChallengerUseCase getChallengerUseCase;
    private final GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Override
    public Optional<ApplicationFormInfo> findByProjectId(Long projectId, Long requesterMemberId) {
        return loadApplicationFormPort.findByProjectId(projectId)
            .map(applicationForm -> assemble(applicationForm, requesterMemberId));
    }

    private ApplicationFormInfo assemble(ProjectApplicationForm applicationForm, Long requesterMemberId) {
        Project project = applicationForm.getProject();
        FormWithStructureInfo formStructure = getFormUseCase.getFormWithStructure(applicationForm.getFormId());
        List<ProjectApplicationFormPolicy> policies =
            loadPolicyPort.listByApplicationFormId(applicationForm.getId());

        if (canViewFullForm(project, requesterMemberId)) {
            return ApplicationFormInfo.of(applicationForm, formStructure, policies);
        }

        ChallengerPart applicantPart = getChallengerUseCase
            .findByMemberIdAndGisuId(requesterMemberId, project.getGisuId())
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.APPLICATION_FORM_ACCESS_NOT_ALLOWED))
            .part();

        return ApplicationFormInfo.forApplicant(applicationForm, formStructure, policies, applicantPart);
    }

    /**
     * 정책 우회 가능 여부. PM(owner) / Central Core / 프로젝트 지부의 지부장 만 전체 섹션을 본다. {@code ProjectPermissionEvaluator#canEdit} 의 외부
     * 운영진 정의와 정합을 맞춘다.
     */
    private boolean canViewFullForm(Project project, Long requesterMemberId) {
        if (Objects.equals(requesterMemberId, project.getProductOwnerMemberId())) {
            return true;
        }
        if (getChallengerRoleUseCase.isCentralCoreInGisu(requesterMemberId, project.getGisuId())) {
            return true;
        }
        return getChallengerRoleUseCase.isChapterPresidentInGisu(
            requesterMemberId, project.getGisuId(), project.getChapterId());
    }
}
