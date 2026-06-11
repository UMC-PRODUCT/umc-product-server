package com.umc.product.organization.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.organization.application.port.out.query.LoadProductTeamFunctionalMembershipPort;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
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
    LoadProductTeamFunctionalMembershipPort loadProductTeamFunctionalMembershipPort;

    @InjectMocks
    ProductTeamAccessPolicy sut;

    @Test
    void 중앙_총괄단은_프로덕트팀_조직을_관리할_수_있다() {
        given(getChallengerRoleUseCase.isCentralCore(1L)).willReturn(true);

        assertThat(sut.canManageProductTeam(1L)).isTrue();
    }

    @Test
    void 활성_프로덕트팀_기수의_PRODUCT_LEAD는_프로덕트팀_조직을_관리할_수_있다() {
        given(getChallengerRoleUseCase.isCentralCore(1L)).willReturn(false);
        given(loadProductTeamFunctionalMembershipPort.existsByMemberIdAndActiveGenerationAndRoles(
            1L,
            Set.of(ProductTeamFunctionalRole.PRODUCT_LEAD, ProductTeamFunctionalRole.PRODUCT_VICE_LEAD)
        )).willReturn(true);

        assertThat(sut.canManageProductTeam(1L)).isTrue();
    }

    @Test
    void 일반_팀원은_프로덕트팀_조직을_관리할_수_없다() {
        given(getChallengerRoleUseCase.isCentralCore(1L)).willReturn(false);
        given(loadProductTeamFunctionalMembershipPort.existsByMemberIdAndActiveGenerationAndRoles(
            1L,
            Set.of(ProductTeamFunctionalRole.PRODUCT_LEAD, ProductTeamFunctionalRole.PRODUCT_VICE_LEAD)
        )).willReturn(false);

        assertThat(sut.canManageProductTeam(1L)).isFalse();
    }
}
