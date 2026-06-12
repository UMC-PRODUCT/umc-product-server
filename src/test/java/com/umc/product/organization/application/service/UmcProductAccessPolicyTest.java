package com.umc.product.organization.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.umc.product.authorization.application.port.in.query.GetChallengerRoleUseCase;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalMembershipPort;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;

@ExtendWith(MockitoExtension.class)
class UmcProductAccessPolicyTest {

    @Mock
    GetChallengerRoleUseCase getChallengerRoleUseCase;

    @Mock
    LoadUmcProductFunctionalMembershipPort loadUmcProductFunctionalMembershipPort;

    @InjectMocks
    UmcProductAccessPolicy sut;

    @Test
    void 중앙_총괄단은_UMC_Product_조직을_관리할_수_있다() {
        given(getChallengerRoleUseCase.isCentralCore(1L)).willReturn(true);

        assertThat(sut.canManageUmcProduct(1L)).isTrue();
    }

    @Test
    void 활성_UMC_Product_기수의_UMC_PRODUCT_LEAD는_UMC_Product_조직을_관리할_수_있다() {
        given(getChallengerRoleUseCase.isCentralCore(1L)).willReturn(false);
        given(loadUmcProductFunctionalMembershipPort.existsByMemberIdAndActiveGenerationAndRoles(
            1L,
            Set.of(UmcProductFunctionalRole.UMC_PRODUCT_LEAD, UmcProductFunctionalRole.UMC_PRODUCT_VICE_LEAD)
        )).willReturn(true);

        assertThat(sut.canManageUmcProduct(1L)).isTrue();
    }

    @Test
    void 일반_팀원은_UMC_Product_조직을_관리할_수_없다() {
        given(getChallengerRoleUseCase.isCentralCore(1L)).willReturn(false);
        given(loadUmcProductFunctionalMembershipPort.existsByMemberIdAndActiveGenerationAndRoles(
            1L,
            Set.of(UmcProductFunctionalRole.UMC_PRODUCT_LEAD, UmcProductFunctionalRole.UMC_PRODUCT_VICE_LEAD)
        )).willReturn(false);

        assertThat(sut.canManageUmcProduct(1L)).isFalse();
    }
}
