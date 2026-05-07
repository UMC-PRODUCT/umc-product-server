package com.umc.product.figma.application.service.evaluator;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FigmaPermissionEvaluator")
class FigmaPermissionEvaluatorTest {

    private final FigmaPermissionEvaluator sut = new FigmaPermissionEvaluator();

    @Test
    @DisplayName("supportedResourceType은 FIGMA를 반환한다")
    void supportedResourceType은_FIGMA를_반환한다() {
        assertThat(sut.supportedResourceType()).isEqualTo(ResourceType.FIGMA);
    }

    @Test
    @DisplayName("SUPER_ADMIN 은 READ 권한을 통과한다")
    void SUPER_ADMIN_READ_허용() {
        SubjectAttributes subject = subjectWithRoles(superAdminRole());
        ResourcePermission permission = ResourcePermission.ofType(ResourceType.FIGMA, PermissionType.READ);

        assertThat(sut.evaluate(subject, permission)).isTrue();
    }

    @Test
    @DisplayName("SUPER_ADMIN 은 MANAGE 권한을 통과한다")
    void SUPER_ADMIN_MANAGE_허용() {
        SubjectAttributes subject = subjectWithRoles(superAdminRole());
        ResourcePermission permission = ResourcePermission.ofType(ResourceType.FIGMA, PermissionType.MANAGE);

        assertThat(sut.evaluate(subject, permission)).isTrue();
    }

    @Test
    @DisplayName("중앙운영사무국 총괄(CENTRAL_PRESIDENT) 도 READ 권한을 거부한다")
    void 총괄단_READ_거부() {
        SubjectAttributes subject = subjectWithRoles(roleOf(ChallengerRoleType.CENTRAL_PRESIDENT));
        ResourcePermission permission = ResourcePermission.ofType(ResourceType.FIGMA, PermissionType.READ);

        assertThat(sut.evaluate(subject, permission)).isFalse();
    }

    @Test
    @DisplayName("학교 회장(SCHOOL_PRESIDENT) 은 MANAGE 권한을 거부한다")
    void 학교_회장_MANAGE_거부() {
        SubjectAttributes subject = subjectWithRoles(roleOf(ChallengerRoleType.SCHOOL_PRESIDENT));
        ResourcePermission permission = ResourcePermission.ofType(ResourceType.FIGMA, PermissionType.MANAGE);

        assertThat(sut.evaluate(subject, permission)).isFalse();
    }

    @Test
    @DisplayName("어떤 역할도 없는 사용자는 READ 권한을 거부한다")
    void 역할_없음_READ_거부() {
        SubjectAttributes subject = subjectWithRoles();
        ResourcePermission permission = ResourcePermission.ofType(ResourceType.FIGMA, PermissionType.READ);

        assertThat(sut.evaluate(subject, permission)).isFalse();
    }

    @Test
    @DisplayName("여러 역할을 가진 사용자 중 하나라도 SUPER_ADMIN 이면 통과한다")
    void 다중_역할_SUPER_ADMIN_포함_허용() {
        SubjectAttributes subject = subjectWithRoles(
            roleOf(ChallengerRoleType.SCHOOL_PART_LEADER),
            superAdminRole()
        );
        ResourcePermission permission = ResourcePermission.ofType(ResourceType.FIGMA, PermissionType.MANAGE);

        assertThat(sut.evaluate(subject, permission)).isTrue();
    }

    private SubjectAttributes subjectWithRoles(RoleAttribute... roles) {
        return SubjectAttributes.builder()
            .memberId(1L)
            .schoolId(1L)
            .gisuChallengerInfos(List.of())
            .roleAttributes(List.of(roles))
            .build();
    }

    private RoleAttribute superAdminRole() {
        return roleOf(ChallengerRoleType.SUPER_ADMIN);
    }

    private RoleAttribute roleOf(ChallengerRoleType roleType) {
        return new RoleAttribute(
            roleType,
            OrganizationType.CENTRAL,
            null,
            null,
            1L
        );
    }
}
