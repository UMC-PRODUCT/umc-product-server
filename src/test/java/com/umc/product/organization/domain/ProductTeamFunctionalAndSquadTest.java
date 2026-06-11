package com.umc.product.organization.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamSquadRole;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ProductTeamFunctionalAndSquadTest {

    @Test
    void 같은_기수에서_챕터와_파트_멤버십을_동시에_가질_수_있다() {
        ProductTeamGeneration generation = ProductTeamGeneration.create(
            1L,
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-12-31T23:59:59Z"),
            true
        );
        ReflectionTestUtils.setField(generation, "id", 10L);
        ProductTeamMember member = ProductTeamMember.create(100L, "서버와 클라이언트를 함께 봅니다.", "product-profile");
        ProductTeamFunctionalUnit clientChapter = functionalUnit(
            1L,
            generation.getId(),
            null,
            ProductTeamFunctionalUnitType.CHAPTER,
            "CLIENT",
            "클라이언트 챕터"
        );
        ProductTeamFunctionalUnit serverPart = functionalUnit(
            2L,
            generation.getId(),
            null,
            ProductTeamFunctionalUnitType.PART,
            "SERVER",
            "Server 파트"
        );

        ProductTeamFunctionalMembership chapterMembership = ProductTeamFunctionalMembership.create(
            member,
            generation.getId(),
            clientChapter.getId(),
            ProductTeamFunctionalRole.MEMBER,
            ProductTeamPosition.UNSPECIFIED,
            "클라이언트 표준 지원",
            "클라이언트 챕터 운영을 지원합니다."
        );
        ProductTeamFunctionalMembership partMembership = ProductTeamFunctionalMembership.create(
            member,
            generation.getId(),
            serverPart.getId(),
            ProductTeamFunctionalRole.PART_LEAD,
            ProductTeamPosition.SERVER_DEVELOPER,
            "API 설계",
            "Server 파트의 API 계약과 리뷰를 담당합니다."
        );

        assertThat(chapterMembership.getFunctionalUnitId()).isEqualTo(clientChapter.getId());
        assertThat(partMembership.getFunctionalUnitId()).isEqualTo(serverPart.getId());
        assertThat(partMembership.getRole()).isEqualTo(ProductTeamFunctionalRole.PART_LEAD);
        assertThat(partMembership.getPosition()).isEqualTo(ProductTeamPosition.SERVER_DEVELOPER);
        assertThat(partMembership.getResponsibilityTitle()).isEqualTo("API 설계");
    }

    @Test
    void Squad는_기수_없이_기간을_비워서_생성할_수_있다() {
        ProductTeamSquad squad = ProductTeamSquad.create(
            "RECRUIT",
            "모집 Squad",
            "모집 기능을 개선합니다.",
            null,
            null,
            1,
            true
        );

        assertThat(squad.getCode()).isEqualTo("RECRUIT");
        assertThat(squad.getStartAt()).isNull();
        assertThat(squad.getEndAt()).isNull();
    }

    @Test
    void Squad_기간이_둘_다_있으면_시작일은_종료일보다_앞서야_한다() {
        assertThatThrownBy(() -> ProductTeamSquad.create(
            "INVALID",
            "잘못된 Squad",
            null,
            Instant.parse("2026-03-01T00:00:00Z"),
            Instant.parse("2026-03-01T00:00:00Z"),
            1,
            true
        ))
            .isInstanceOf(BusinessException.class)
            .extracting("baseCode")
            .isEqualTo(OrganizationErrorCode.PRODUCT_TEAM_SQUAD_PERIOD_INVALID);
    }

    @Test
    void Squad_참여자는_SQUAD_LEAD와_담당_범위를_가질_수_있다() {
        ProductTeamSquad squad = squad(1L);
        ProductTeamMember member = ProductTeamMember.create(100L, "소개", null);

        ProductTeamSquadParticipant participant = ProductTeamSquadParticipant.create(
            squad,
            member,
            ProductTeamSquadRole.SQUAD_LEAD,
            ProductTeamPosition.PRODUCT_OWNER,
            "모집 정책 정리",
            "요구사항과 QA 범위를 관리합니다."
        );

        assertThat(participant.getSquad()).isSameAs(squad);
        assertThat(participant.getRole()).isEqualTo(ProductTeamSquadRole.SQUAD_LEAD);
        assertThat(participant.getResponsibilityTitle()).isEqualTo("모집 정책 정리");
    }

    private ProductTeamFunctionalUnit functionalUnit(
        Long id,
        Long generationId,
        Long parentUnitId,
        ProductTeamFunctionalUnitType type,
        String code,
        String name
    ) {
        ProductTeamFunctionalUnit unit = ProductTeamFunctionalUnit.create(
            generationId,
            parentUnitId,
            type,
            code,
            name,
            null,
            1,
            true
        );
        ReflectionTestUtils.setField(unit, "id", id);
        return unit;
    }

    private ProductTeamSquad squad(Long id) {
        ProductTeamSquad squad = ProductTeamSquad.create(
            "RECRUIT",
            "모집 Squad",
            null,
            null,
            null,
            1,
            true
        );
        ReflectionTestUtils.setField(squad, "id", id);
        return squad;
    }
}
