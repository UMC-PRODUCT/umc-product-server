package com.umc.product.authorization.application.service.evaluator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SubjectAttributes.GisuChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;
import com.umc.product.project.domain.exception.ProjectDomainException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectPermissionEvaluatorTest {

    @Mock
    LoadProjectPort loadProjectPort;

    @InjectMocks
    ProjectPermissionEvaluator sut;

    @Test
    void supportedResourceType은_PROJECT를_반환한다() {
        assertThat(sut.supportedResourceType()).isEqualTo(ResourceType.PROJECT);
    }

    // --- READ ---

    @Test
    void READ는_모든_인증된_사용자_허용() {
        SubjectAttributes subject = subjectWith(1L, List.of(), List.of());
        ResourcePermission permission = ResourcePermission.ofType(ResourceType.PROJECT, PermissionType.READ);

        assertThat(sut.evaluate(subject, permission)).isTrue();
    }

    // --- WRITE ---

    @Test
    void WRITE는_PLAN_파트_챌린저_허용() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(gisuInfo(1L, 1L, ChallengerPart.PLAN, 1L)),
            List.of());
        ResourcePermission permission = ResourcePermission.ofType(ResourceType.PROJECT, PermissionType.WRITE);

        assertThat(sut.evaluate(subject, permission)).isTrue();
    }

    @Test
    void WRITE는_비PLAN_파트_거부() {
        SubjectAttributes subject = subjectWith(1L,
            List.of(gisuInfo(1L, 1L, ChallengerPart.SPRINGBOOT, 1L)),
            List.of());
        ResourcePermission permission = ResourcePermission.ofType(ResourceType.PROJECT, PermissionType.WRITE);

        assertThat(sut.evaluate(subject, permission)).isFalse();
    }

    @Test
    void WRITE는_챌린저_정보_없으면_거부() {
        SubjectAttributes subject = subjectWith(1L, List.of(), List.of());
        ResourcePermission permission = ResourcePermission.ofType(ResourceType.PROJECT, PermissionType.WRITE);

        assertThat(sut.evaluate(subject, permission)).isFalse();
    }

    // --- EDIT ---

    @Test
    void EDIT은_DRAFT에서_작성자만_허용() {
        Long memberId = 10L;
        Long projectId = 100L;
        given(loadProjectPort.findById(projectId))
            .willReturn(Optional.of(project(projectId, memberId, ProjectStatus.DRAFT)));

        SubjectAttributes subject = subjectWith(memberId, List.of(), List.of());
        ResourcePermission permission = ResourcePermission.of(ResourceType.PROJECT, projectId, PermissionType.EDIT);

        assertThat(sut.evaluate(subject, permission)).isTrue();
    }

    @Test
    void EDIT은_DRAFT에서_작성자가_아니면_거부() {
        Long projectId = 100L;
        given(loadProjectPort.findById(projectId))
            .willReturn(Optional.of(project(projectId, 10L, ProjectStatus.DRAFT)));

        SubjectAttributes subject = subjectWith(20L, List.of(), List.of());
        ResourcePermission permission = ResourcePermission.of(ResourceType.PROJECT, projectId, PermissionType.EDIT);

        assertThat(sut.evaluate(subject, permission)).isFalse();
    }

    @Test
    void EDIT은_DRAFT에서_중앙총괄이라도_작성자가_아니면_거부() {
        Long projectId = 100L;
        given(loadProjectPort.findById(projectId))
            .willReturn(Optional.of(project(projectId, 10L, ProjectStatus.DRAFT)));

        SubjectAttributes subject = subjectWith(20L, List.of(), List.of(centralCoreRole()));
        ResourcePermission permission = ResourcePermission.of(ResourceType.PROJECT, projectId, PermissionType.EDIT);

        assertThat(sut.evaluate(subject, permission)).isFalse();
    }

    @Test
    void EDIT은_PENDING_REVIEW에서_작성자_허용() {
        Long memberId = 10L;
        Long projectId = 100L;
        given(loadProjectPort.findById(projectId))
            .willReturn(Optional.of(project(projectId, memberId, ProjectStatus.PENDING_REVIEW)));

        SubjectAttributes subject = subjectWith(memberId, List.of(), List.of());
        ResourcePermission permission = ResourcePermission.of(ResourceType.PROJECT, projectId, PermissionType.EDIT);

        assertThat(sut.evaluate(subject, permission)).isTrue();
    }

    @Test
    void EDIT은_PENDING_REVIEW에서_중앙총괄_허용() {
        Long projectId = 100L;
        given(loadProjectPort.findById(projectId))
            .willReturn(Optional.of(project(projectId, 10L, ProjectStatus.PENDING_REVIEW)));

        SubjectAttributes subject = subjectWith(20L, List.of(), List.of(centralCoreRole()));
        ResourcePermission permission = ResourcePermission.of(ResourceType.PROJECT, projectId, PermissionType.EDIT);

        assertThat(sut.evaluate(subject, permission)).isTrue();
    }

    @Test
    void EDIT은_IN_PROGRESS에서_중앙총괄_허용() {
        Long projectId = 100L;
        given(loadProjectPort.findById(projectId))
            .willReturn(Optional.of(project(projectId, 10L, ProjectStatus.IN_PROGRESS)));

        SubjectAttributes subject = subjectWith(20L, List.of(), List.of(centralCoreRole()));
        ResourcePermission permission = ResourcePermission.of(ResourceType.PROJECT, projectId, PermissionType.EDIT);

        assertThat(sut.evaluate(subject, permission)).isTrue();
    }

    @Test
    void EDIT은_COMPLETED_상태에서_거부() {
        Long memberId = 10L;
        Long projectId = 100L;
        given(loadProjectPort.findById(projectId))
            .willReturn(Optional.of(project(projectId, memberId, ProjectStatus.COMPLETED)));

        SubjectAttributes subject = subjectWith(memberId, List.of(), List.of());
        ResourcePermission permission = ResourcePermission.of(ResourceType.PROJECT, projectId, PermissionType.EDIT);

        assertThat(sut.evaluate(subject, permission)).isFalse();
    }

    @Test
    void EDIT은_ABORTED_상태에서_거부() {
        Long memberId = 10L;
        Long projectId = 100L;
        given(loadProjectPort.findById(projectId))
            .willReturn(Optional.of(project(projectId, memberId, ProjectStatus.ABORTED)));

        SubjectAttributes subject = subjectWith(memberId, List.of(), List.of());
        ResourcePermission permission = ResourcePermission.of(ResourceType.PROJECT, projectId, PermissionType.EDIT);

        assertThat(sut.evaluate(subject, permission)).isFalse();
    }

    @Test
    void EDIT은_프로젝트가_없으면_예외() {
        Long projectId = 999L;
        given(loadProjectPort.findById(projectId)).willReturn(Optional.empty());

        SubjectAttributes subject = subjectWith(1L, List.of(), List.of());
        ResourcePermission permission = ResourcePermission.of(ResourceType.PROJECT, projectId, PermissionType.EDIT);

        assertThatThrownBy(() -> sut.evaluate(subject, permission))
            .isInstanceOf(ProjectDomainException.class);
    }

    // --- DELETE ---

    @Test
    void DELETE는_중앙총괄만_허용() {
        SubjectAttributes subject = subjectWith(1L, List.of(), List.of(centralCoreRole()));
        ResourcePermission permission = ResourcePermission.ofType(ResourceType.PROJECT, PermissionType.DELETE);

        assertThat(sut.evaluate(subject, permission)).isTrue();
    }

    @Test
    void DELETE는_일반_사용자_거부() {
        SubjectAttributes subject = subjectWith(1L, List.of(), List.of());
        ResourcePermission permission = ResourcePermission.ofType(ResourceType.PROJECT, PermissionType.DELETE);

        assertThat(sut.evaluate(subject, permission)).isFalse();
    }

    // --- helpers ---

    private SubjectAttributes subjectWith(Long memberId,
                                          List<GisuChallengerInfo> gisuInfos,
                                          List<RoleAttribute> roles) {
        return SubjectAttributes.builder()
            .memberId(memberId)
            .schoolId(1L)
            .gisuChallengerInfos(gisuInfos)
            .roleAttributes(roles)
            .build();
    }

    private GisuChallengerInfo gisuInfo(Long gisuId, Long chapterId,
                                        ChallengerPart part, Long challengerId) {
        return GisuChallengerInfo.builder()
            .gisuId(gisuId)
            .chapterId(chapterId)
            .part(part)
            .challengerId(challengerId)
            .build();
    }

    private RoleAttribute centralCoreRole() {
        return new RoleAttribute(
            ChallengerRoleType.CENTRAL_PRESIDENT,
            OrganizationType.CENTRAL,
            null, null, 1L
        );
    }

    private Project project(Long id, Long ownerMemberId, ProjectStatus status) {
        try {
            var constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Project project = constructor.newInstance();
            ReflectionTestUtils.setField(project, "id", id);
            ReflectionTestUtils.setField(project, "productOwnerMemberId", ownerMemberId);
            ReflectionTestUtils.setField(project, "status", status);
            ReflectionTestUtils.setField(project, "gisuId", 1L);
            ReflectionTestUtils.setField(project, "chapterId", 1L);
            return project;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
