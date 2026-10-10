package com.umc.product.challenger.application.service.evaluator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourcePermission;
import com.umc.product.authorization.domain.ResourceType;
import com.umc.product.authorization.domain.RoleAttribute;
import com.umc.product.authorization.domain.SubjectAttributes;
import com.umc.product.authorization.domain.SystemRoleType;
import com.umc.product.challenger.application.port.in.query.GetChallengerPointUseCase;
import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerPointInfo;
import com.umc.product.common.domain.enums.ChallengerRoleType;
import com.umc.product.common.domain.enums.OrganizationType;
import com.umc.product.member.application.port.in.query.GetMemberUseCase;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengerPointPermissionEvaluator")
class ChallengerPointPermissionEvaluatorTest {

    private static final Long CHALLENGER_POINT_ID = 50L;
    private static final Long CHALLENGER_ID = 40L;
    private static final Long TARGET_GISU_ID = 11L;
    @Mock
    GetChallengerUseCase getChallengerUseCase;

    @Mock
    GetMemberUseCase getMemberUseCase;

    @Mock
    GetChallengerPointUseCase getChallengerPointUseCase;

    @Test
    @DisplayName("대상 기수의 중앙 총괄단은 상벌점을 삭제할 수 있다")
    void central_core_in_target_gisu_can_delete_challenger_point() {
        givenDeleteTarget();

        assertThat(evaluateDelete(subjectWithCentralCore(TARGET_GISU_ID))).isTrue();
    }

    @Test
    @DisplayName("다른 기수의 중앙 총괄단은 상벌점을 삭제할 수 없다")
    void central_core_in_other_gisu_cannot_delete_challenger_point() {
        givenDeleteTarget();

        assertThat(evaluateDelete(subjectWithCentralCore(9L))).isFalse();
    }

    @Test
    @DisplayName("SUPER_ADMIN은 기수와 관계없이 상벌점을 삭제할 수 있다")
    void super_admin_can_delete_challenger_point() {
        givenDeleteTarget();
        SubjectAttributes subject = SubjectAttributes.builder()
            .memberId(1L)
            .gisuChallengerInfos(List.of())
            .roleAttributes(List.of())
            .systemRoles(Set.of(SystemRoleType.SUPER_ADMIN))
            .build();

        assertThat(evaluateDelete(subject)).isTrue();
    }

    private void givenDeleteTarget() {
        given(getChallengerPointUseCase.getById(CHALLENGER_POINT_ID)).willReturn(ChallengerPointInfo.builder()
            .id(CHALLENGER_POINT_ID)
            .challengerId(CHALLENGER_ID)
            .build());
        given(getChallengerUseCase.getById(CHALLENGER_ID)).willReturn(ChallengerInfo.builder()
            .challengerId(CHALLENGER_ID)
            .gisuId(TARGET_GISU_ID)
            .build());
    }

    private boolean evaluateDelete(SubjectAttributes subject) {
        ChallengerPointPermissionEvaluator sut = new ChallengerPointPermissionEvaluator(
            getChallengerUseCase,
            getMemberUseCase,
            getChallengerPointUseCase
        );
        return sut.evaluate(
            subject,
            ResourcePermission.of(ResourceType.CHALLENGER_POINT, CHALLENGER_POINT_ID, PermissionType.DELETE)
        );
    }

    private SubjectAttributes subjectWithCentralCore(Long gisuId) {
        return SubjectAttributes.builder()
            .memberId(1L)
            .schoolId(30L)
            .gisuChallengerInfos(List.of())
            .roleAttributes(List.of(new RoleAttribute(
                ChallengerRoleType.CENTRAL_VICE_PRESIDENT,
                OrganizationType.CENTRAL,
                null,
                null,
                gisuId
            )))
            .systemRoles(Set.of())
            .build();
    }
}
