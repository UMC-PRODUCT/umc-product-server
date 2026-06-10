package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.common.domain.enums.MemberRoleType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberAdminPermissionEvaluatorTest {

    private final MemberPermissionEvaluator sut = new MemberPermissionEvaluator();

    @Test
    @DisplayName("챌린저 기록이 없는 ADMIN은 회원 관리 권한을 가진다")
    void 챌린저_기록_없는_ADMIN은_회원_관리_가능() {
        SubjectAttributes subject = subject(MemberRoleType.ADMIN);

        boolean result = sut.evaluate(subject, ResourcePermission.ofType(ResourceType.MEMBER, PermissionType.MANAGE));

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("챌린저 기록이 없는 NORMAL은 회원 관리 권한이 없다")
    void 챌린저_기록_없는_NORMAL은_회원_관리_불가() {
        SubjectAttributes subject = subject(MemberRoleType.NORMAL);

        boolean result = sut.evaluate(subject, ResourcePermission.ofType(ResourceType.MEMBER, PermissionType.MANAGE));

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("챌린저 기록이 없는 ADMIN은 관리자 회원 삭제 권한을 가진다")
    void 챌린저_기록_없는_ADMIN은_회원_삭제_가능() {
        SubjectAttributes subject = subject(MemberRoleType.ADMIN);

        boolean result = sut.evaluate(subject, ResourcePermission.of(ResourceType.MEMBER, 2L, PermissionType.DELETE));

        assertThat(result).isTrue();
    }

    private SubjectAttributes subject(MemberRoleType roleType) {
        return SubjectAttributes.builder()
            .memberId(1L)
            .schoolId(1L)
            .memberRoleType(roleType)
            .gisuChallengerInfos(List.of())
            .roleAttributes(List.of())
            .build();
    }
}
