package com.umc.product.project.application.service.evaluator;

import com.umc.product.authorization.application.port.out.ResourcePermissionEvaluator;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.exception.ProjectDomainException;
import com.umc.product.project.domain.exception.ProjectErrorCode;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ProjectApplication 도메인 단건 액션의 권한 판정 (L2). subject × resource 속성 매칭만 다룬다.
 * <p>
 * 도메인 규칙(부모 프로젝트 status, 매칭 라운드 OPEN, 폼 정책 파트, 기수 ACTIVE 멤버, 동일 라운드 중복)은
 * {@code Project.validateApplicable()} / {@code ProjectApplicationCommandService} 가 책임진다.
 */
@Component
@RequiredArgsConstructor
public class ProjectApplicationPermissionEvaluator implements ResourcePermissionEvaluator {

    private final LoadProjectPort loadProjectPort;

    @Override
    public ResourceType supportedResourceType() {
        return ResourceType.PROJECT_APPLICATION;
    }

    @Override
    public boolean evaluate(SubjectAttributes subject, ResourcePermission permission) {
        return switch (permission.permission()) {
            case WRITE -> canWrite(subject, permission);
            default -> false;
        };
    }

    /**
     * 지원서 생성/임시저장/제출 진입 권한.
     * <p>
     * resourceId 는 부모 프로젝트의 ID. 단순 컨벤션상 비대칭이지만 기수 매칭/자기지원 차단을 L1 에서 처리하기 위함.
     * <ul>
     *   <li>부모 프로젝트의 기수에 챌린저 레코드가 있어야 함 (subject × resource 속성 매칭)</li>
     *   <li>호출자가 부모 프로젝트의 PO 본인이면 차단 (자기지원 방지)</li>
     * </ul>
     * 부모 프로젝트의 status (IN_PROGRESS) / 매칭 라운드 OPEN / 폼 정책 파트 / 기수 ACTIVE 멤버 / 중복 등 도메인 규칙은 Service & Domain 가 검증한다.
     */
    private boolean canWrite(SubjectAttributes subject, ResourcePermission permission) {
        Project project = loadProject(permission);

        if (Objects.equals(subject.memberId(), project.getProductOwnerMemberId())) {
            return false;
        }

        return subject.gisuChallengerInfos().stream()
            .anyMatch(info -> Objects.equals(info.gisuId(), project.getGisuId()));
    }

    private Project loadProject(ResourcePermission permission) {
        Long projectId = permission.getResourceIdAsLong();
        return loadProjectPort.findById(projectId)
            .orElseThrow(() -> new ProjectDomainException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }
}
