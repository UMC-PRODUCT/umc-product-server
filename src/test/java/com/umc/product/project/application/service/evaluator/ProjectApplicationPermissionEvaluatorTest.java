package com.umc.product.project.application.service.evaluator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SubjectAttributes.GisuChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.project.application.port.out.LoadProjectPort;
import com.umc.product.project.domain.Project;
import com.umc.product.project.domain.enums.ProjectStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationPermissionEvaluatorTest {

    private static final Long PROJECT_GISU_ID = 1L;
    private static final Long PROJECT_CHAPTER_ID = 1L;
    private static final Long PROJECT_ID = 100L;
    private static final Long PO_MEMBER_ID = 10L;
    private static final Long APPLICANT_MEMBER_ID = 20L;

    @Mock
    LoadProjectPort loadProjectPort;

    @InjectMocks
    ProjectApplicationPermissionEvaluator sut;

    @Test
    void supportedResourceType은_PROJECT_APPLICATION을_반환한다() {
        assertThat(sut.supportedResourceType()).isEqualTo(ResourceType.PROJECT_APPLICATION);
    }

    // --- WRITE (resourceId = projectId) ---

    @Test
    void WRITE는_같은_기수_챌린저_허용() {
        givenProject(ProjectStatus.IN_PROGRESS);
        SubjectAttributes subject = subjectWith(APPLICANT_MEMBER_ID,
            List.of(gisuInfo(PROJECT_GISU_ID, PROJECT_CHAPTER_ID, ChallengerPart.SPRINGBOOT, 99L)),
            List.of());

        assertThat(sut.evaluate(subject, writePermission())).isTrue();
    }

    @Test
    void WRITE는_PO_본인이면_거부_자기지원_차단() {
        givenProject(ProjectStatus.IN_PROGRESS);
        SubjectAttributes subject = subjectWith(PO_MEMBER_ID,
            List.of(gisuInfo(PROJECT_GISU_ID, PROJECT_CHAPTER_ID, ChallengerPart.PLAN, 99L)),
            List.of());

        assertThat(sut.evaluate(subject, writePermission())).isFalse();
    }

    @Test
    void WRITE는_다른_기수_챌린저_거부() {
        givenProject(ProjectStatus.IN_PROGRESS);
        SubjectAttributes subject = subjectWith(APPLICANT_MEMBER_ID,
            List.of(gisuInfo(99L, PROJECT_CHAPTER_ID, ChallengerPart.SPRINGBOOT, 99L)),
            List.of());

        assertThat(sut.evaluate(subject, writePermission())).isFalse();
    }

    @Test
    void WRITE는_챌린저_정보_없으면_거부() {
        givenProject(ProjectStatus.IN_PROGRESS);
        SubjectAttributes subject = subjectWith(APPLICANT_MEMBER_ID, List.of(), List.of());

        assertThat(sut.evaluate(subject, writePermission())).isFalse();
    }

    // --- helpers ---

    private void givenProject(ProjectStatus status) {
        given(loadProjectPort.findById(PROJECT_ID))
            .willReturn(Optional.of(project(PROJECT_ID, PO_MEMBER_ID, status)));
    }

    private ResourcePermission writePermission() {
        return ResourcePermission.of(ResourceType.PROJECT_APPLICATION, PROJECT_ID, PermissionType.WRITE);
    }

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

    private Project project(Long id, Long ownerMemberId, ProjectStatus status) {
        try {
            var constructor = Project.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Project project = constructor.newInstance();
            ReflectionTestUtils.setField(project, "id", id);
            ReflectionTestUtils.setField(project, "productOwnerMemberId", ownerMemberId);
            ReflectionTestUtils.setField(project, "createdByMemberId", ownerMemberId);
            ReflectionTestUtils.setField(project, "status", status);
            ReflectionTestUtils.setField(project, "gisuId", PROJECT_GISU_ID);
            ReflectionTestUtils.setField(project, "chapterId", PROJECT_CHAPTER_ID);
            return project;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
