package com.umc.product.organization.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMembershipPort;
import com.umc.product.organization.domain.enums.ProductTeamRole;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductTeamAccessPolicyTest {

    @Mock
    GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Mock
    LoadProductTeamMembershipPort loadProductTeamMembershipPort;

    @InjectMocks
    ProductTeamAccessPolicy sut;

    @Test
    void 중앙_총괄단은_프로덕트팀_기수를_생성할_수_있다() {
        given(getChallengerRoleUseCase.isCentralCore(1L)).willReturn(true);

        assertThat(sut.canCreateGeneration(1L)).isTrue();
    }

    @Test
    void 해당_프로덕트팀_기수의_PRODUCT_LEAD는_기수와_멤버를_관리할_수_있다() {
        given(getChallengerRoleUseCase.isCentralCore(1L)).willReturn(false);
        given(loadProductTeamMembershipPort.existsByMemberIdAndGenerationIdAndRoles(
            1L,
            10L,
            Set.of(ProductTeamRole.PRODUCT_LEAD, ProductTeamRole.PRODUCT_VICE_LEAD, ProductTeamRole.GENERAL_MANAGER)
        )).willReturn(true);

        assertThat(sut.canManageGeneration(1L, 10L)).isTrue();
    }

    @Test
    void 일반_팀원은_프로덕트팀_활동을_관리할_수_없다() {
        given(getChallengerRoleUseCase.isCentralCore(1L)).willReturn(false);
        given(loadProductTeamMembershipPort.existsByMemberIdAndGenerationIdAndRoles(
            1L,
            10L,
            Set.of(ProductTeamRole.PRODUCT_LEAD, ProductTeamRole.PRODUCT_VICE_LEAD, ProductTeamRole.GENERAL_MANAGER)
        )).willReturn(false);

        assertThat(sut.canManageGeneration(1L, 10L)).isFalse();
    }
}
