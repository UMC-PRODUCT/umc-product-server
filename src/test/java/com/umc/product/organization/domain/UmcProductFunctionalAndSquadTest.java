package com.umc.product.organization.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.umc.product.global.exception.BusinessException;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import com.umc.product.organization.domain.enums.UmcProductPosition;
import com.umc.product.organization.domain.enums.UmcProductSquadRole;
import com.umc.product.organization.exception.OrganizationErrorCode;

class UmcProductFunctionalAndSquadTest {

    @Test
    void 같은_기수에서_챕터와_파트_멤버십을_동시에_가질_수_있다() {
        UmcProductGeneration generation = UmcProductGeneration.create(
            1L,
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-12-31T23:59:59Z"),
            true
        );
        ReflectionTestUtils.setField(generation, "id", 10L);
        UmcProductMember member = UmcProductMember.create(100L, "서버와 클라이언트를 함께 봅니다.", "product-profile");
        UmcProductFunctionalUnit clientChapter = functionalUnit(
            1L,
            generation.getId(),
            null,
            UmcProductFunctionalUnitType.CHAPTER,
            "CLIENT",
            "클라이언트 챕터"
        );
        UmcProductFunctionalUnit serverPart = functionalUnit(
            2L,
            generation.getId(),
            null,
            UmcProductFunctionalUnitType.PART,
            "SERVER",
            "Server 파트"
        );

        UmcProductFunctionalMembership chapterMembership = UmcProductFunctionalMembership.create(
            member,
            generation.getId(),
            clientChapter.getId(),
            UmcProductFunctionalRole.MEMBER,
            UmcProductPosition.UNSPECIFIED,
            "클라이언트 표준 지원",
            "클라이언트 챕터 운영을 지원합니다."
        );
        UmcProductFunctionalMembership partMembership = UmcProductFunctionalMembership.create(
            member,
            generation.getId(),
            serverPart.getId(),
            UmcProductFunctionalRole.PART_LEAD,
            UmcProductPosition.SERVER_DEVELOPER,
            "API 설계",
            "Server 파트의 API 계약과 리뷰를 담당합니다."
        );

        assertThat(chapterMembership.getFunctionalUnitId()).isEqualTo(clientChapter.getId());
        assertThat(partMembership.getFunctionalUnitId()).isEqualTo(serverPart.getId());
        assertThat(partMembership.getRole()).isEqualTo(UmcProductFunctionalRole.PART_LEAD);
        assertThat(partMembership.getPosition()).isEqualTo(UmcProductPosition.SERVER_DEVELOPER);
        assertThat(partMembership.getResponsibilityTitle()).isEqualTo("API 설계");
    }

    @Test
    void Squad는_기수_없이_기간을_비워서_생성할_수_있다() {
        UmcProductSquad squad = UmcProductSquad.create(
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
        assertThatThrownBy(() -> UmcProductSquad.create(
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
            .isEqualTo(OrganizationErrorCode.UMC_PRODUCT_SQUAD_PERIOD_INVALID);
    }

    @Test
    void Squad_참여자는_SQUAD_LEAD와_담당_범위를_가질_수_있다() {
        UmcProductSquad squad = squad(1L);
        UmcProductMember member = UmcProductMember.create(100L, "소개", null);

        UmcProductSquadParticipant participant = UmcProductSquadParticipant.create(
            squad,
            member,
            UmcProductSquadRole.SQUAD_LEAD,
            UmcProductPosition.PRODUCT_OWNER,
            "모집 정책 정리",
            "요구사항과 QA 범위를 관리합니다."
        );

        assertThat(participant.getSquad()).isSameAs(squad);
        assertThat(participant.getRole()).isEqualTo(UmcProductSquadRole.SQUAD_LEAD);
        assertThat(participant.getResponsibilityTitle()).isEqualTo("모집 정책 정리");
    }

    private UmcProductFunctionalUnit functionalUnit(
        Long id,
        Long generationId,
        Long parentUnitId,
        UmcProductFunctionalUnitType type,
        String code,
        String name
    ) {
        UmcProductFunctionalUnit unit = UmcProductFunctionalUnit.create(
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

    private UmcProductSquad squad(Long id) {
        UmcProductSquad squad = UmcProductSquad.create(
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
