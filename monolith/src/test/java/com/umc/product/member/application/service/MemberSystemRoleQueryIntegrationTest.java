package com.umc.product.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.umc.product.member.application.port.in.query.ListMemberSystemRoleUseCase;
import com.umc.product.member.domain.MemberSystemRoleType;
import com.umc.product.support.IntegrationTestSupport;
import com.umc.product.support.fixture.MemberFixture;

class MemberSystemRoleQueryIntegrationTest extends IntegrationTestSupport {

    @Autowired
    ListMemberSystemRoleUseCase listMemberSystemRoleUseCase;
    @Autowired
    MemberFixture memberFixture;

    @Test
    @DisplayName("UMC_PRODUCT 회원 목록에는 그 역할을 가진 회원만 들고, SUPER_ADMIN만 가진 회원은 빠진다")
    void UMC_PRODUCT_회원_ID_목록() {
        // given
        Long product = memberFixture.일반("product").getId();
        Long superAdmin = memberFixture.일반("super-admin").getId();
        Long both = memberFixture.일반("both").getId();
        memberFixture.일반("member");
        memberFixture.시스템_역할_부여(product, MemberSystemRoleType.UMC_PRODUCT);
        memberFixture.시스템_역할_부여(superAdmin, MemberSystemRoleType.SUPER_ADMIN);
        memberFixture.시스템_역할_부여(both, MemberSystemRoleType.UMC_PRODUCT);
        memberFixture.시스템_역할_부여(both, MemberSystemRoleType.SUPER_ADMIN);

        // when
        List<Long> memberIds = listMemberSystemRoleUseCase.listUmcProductMemberIds();

        // then
        assertThat(memberIds).containsExactlyInAnyOrder(product, both);
    }
}
