package com.umc.product.organization.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.organization.domain.enums.ProductTeamPart;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamRole;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProductTeamMembershipTest {

    @Test
    void 같은_기수에서_여러_직책과_포지션을_겸임할_수_있다() {
        ProductTeamGeneration generation = ProductTeamGeneration.create(
            1L,
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-12-31T23:59:59Z"),
            true
        );
        ReflectionTestUtils.setField(generation, "id", 10L);
        ProductTeamMember member = ProductTeamMember.create(100L, "서버와 모바일을 함께 봅니다.", "product-profile");

        ProductTeamMembership backend = ProductTeamMembership.create(
            member,
            generation.getId(),
            ProductTeamPart.SERVER,
            ProductTeamRole.TEAM_LEADER,
            ProductTeamPosition.BACKEND_DEVELOPER
        );
        ProductTeamMembership android = ProductTeamMembership.create(
            member,
            generation.getId(),
            ProductTeamPart.MOBILE,
            ProductTeamRole.MEMBER,
            ProductTeamPosition.ANDROID_DEVELOPER
        );

        assertThat(backend.getProductTeamMember()).isSameAs(member);
        assertThat(backend.getProductTeamGenerationId()).isEqualTo(generation.getId());
        assertThat(backend.getPart()).isEqualTo(ProductTeamPart.SERVER);
        assertThat(android.getPart()).isEqualTo(ProductTeamPart.MOBILE);
        assertThat(android.getPosition()).isEqualTo(ProductTeamPosition.ANDROID_DEVELOPER);
    }

    @Test
    void 활동_기록에_프로덕트팀_기수는_필수이다() {
        ProductTeamMember member = ProductTeamMember.create(100L, "소개", null);

        assertThatThrownBy(() -> ProductTeamMembership.create(
            member,
            null,
            ProductTeamPart.SERVER,
            ProductTeamRole.MEMBER,
            ProductTeamPosition.BACKEND_DEVELOPER
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("baseCode")
            .isEqualTo(OrganizationErrorCode.PRODUCT_TEAM_GENERATION_REQUIRED);
    }
}
