package com.umc.product.organization.adapter.out.persistence.umcproduct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.global.config.JpaConfig;
import com.umc.product.global.config.QueryDslConfig;
import com.umc.product.organization.domain.UmcProductFunctionalMembership;
import com.umc.product.organization.domain.UmcProductFunctionalUnit;
import com.umc.product.organization.domain.UmcProductGeneration;
import com.umc.product.organization.domain.UmcProductMember;
import com.umc.product.organization.domain.UmcProductSquad;
import com.umc.product.organization.domain.UmcProductSquadParticipant;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import com.umc.product.organization.domain.enums.UmcProductPosition;
import com.umc.product.organization.domain.enums.UmcProductSquadRole;
import com.umc.product.support.TestContainersConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    JpaConfig.class,
    QueryDslConfig.class,
    TestContainersConfig.class,
    UmcProductMemberQueryRepository.class,
    UmcProductGenerationPersistenceAdapter.class,
    UmcProductMemberPersistenceAdapter.class,
    UmcProductFunctionalUnitPersistenceAdapter.class,
    UmcProductFunctionalMembershipPersistenceAdapter.class,
    UmcProductSquadPersistenceAdapter.class,
    UmcProductSquadParticipantPersistenceAdapter.class
})
class UmcProductPersistenceAdapterTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    UmcProductGenerationPersistenceAdapter generationAdapter;

    @Autowired
    UmcProductMemberPersistenceAdapter memberAdapter;

    @Autowired
    UmcProductFunctionalUnitPersistenceAdapter functionalUnitAdapter;

    @Autowired
    UmcProductFunctionalMembershipPersistenceAdapter functionalMembershipAdapter;

    @Autowired
    UmcProductSquadPersistenceAdapter squadAdapter;

    @Autowired
    UmcProductSquadParticipantPersistenceAdapter squadParticipantAdapter;

    @Test
    void 같은_기수에서_챕터와_파트_멤버십을_동시에_가질_수_있다() {
        UmcProductGeneration generation = saveGeneration(1L);
        UmcProductMember member = memberAdapter.save(UmcProductMember.create(100L, "소개", null));
        UmcProductFunctionalUnit chapter = saveFunctionalUnit(generation.getId(), UmcProductFunctionalUnitType.CHAPTER, "CLIENT");
        UmcProductFunctionalUnit part = saveFunctionalUnit(generation.getId(), UmcProductFunctionalUnitType.PART, "SERVER");

        functionalMembershipAdapter.saveAll(List.of(
            UmcProductFunctionalMembership.create(
                member,
                generation.getId(),
                chapter.getId(),
                UmcProductFunctionalRole.MEMBER,
                UmcProductPosition.UNSPECIFIED,
                "챕터 운영 지원",
                null
            ),
            UmcProductFunctionalMembership.create(
                member,
                generation.getId(),
                part.getId(),
                UmcProductFunctionalRole.PART_LEAD,
                UmcProductPosition.SERVER_DEVELOPER,
                "API 설계",
                null
            )
        ));

        em.flush();
        em.clear();

        assertThat(functionalMembershipAdapter.listByUmcProductMemberId(member.getId()))
            .extracting(UmcProductFunctionalMembership::getFunctionalUnitId)
            .containsExactlyInAnyOrder(chapter.getId(), part.getId());
    }

    @Test
    void UMC_PRODUCT_LEAD는_기수당_한_명만_둘_수_있다() {
        UmcProductGeneration generation = saveGeneration(2L);
        UmcProductFunctionalUnit hq = saveFunctionalUnit(generation.getId(), UmcProductFunctionalUnitType.UMC_PRODUCT_HQ, "HQ");
        UmcProductMember first = memberAdapter.save(UmcProductMember.create(101L, "소개", null));
        UmcProductMember second = memberAdapter.save(UmcProductMember.create(102L, "소개", null));

        functionalMembershipAdapter.save(UmcProductFunctionalMembership.create(
            first,
            generation.getId(),
            hq.getId(),
            UmcProductFunctionalRole.UMC_PRODUCT_LEAD,
            UmcProductPosition.PRODUCT_OWNER,
            "UMC Product 리드",
            null
        ));
        assertThatThrownBy(() -> {
            functionalMembershipAdapter.save(UmcProductFunctionalMembership.create(
                second,
                generation.getId(),
                hq.getId(),
                UmcProductFunctionalRole.UMC_PRODUCT_LEAD,
                UmcProductPosition.PRODUCT_OWNER,
                "UMC Product 리드",
                null
            ));
            em.flush();
        })
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void SQUAD_LEAD는_Squad당_한_명만_둘_수_있다() {
        UmcProductSquad squad = squadAdapter.save(UmcProductSquad.create(
            "RECRUIT",
            "모집 Squad",
            null,
            Instant.parse("2026-02-01T00:00:00Z"),
            Instant.parse("2026-03-01T00:00:00Z"),
            1,
            true
        ));
        UmcProductMember first = memberAdapter.save(UmcProductMember.create(103L, "소개", null));
        UmcProductMember second = memberAdapter.save(UmcProductMember.create(104L, "소개", null));

        squadParticipantAdapter.save(UmcProductSquadParticipant.create(
            squad,
            first,
            UmcProductSquadRole.SQUAD_LEAD,
            UmcProductPosition.PRODUCT_OWNER,
            "모집 정책",
            null
        ));
        assertThatThrownBy(() -> {
            squadParticipantAdapter.save(UmcProductSquadParticipant.create(
                squad,
                second,
                UmcProductSquadRole.SQUAD_LEAD,
                UmcProductPosition.PRODUCT_OWNER,
                "모집 정책",
                null
            ));
            em.flush();
        })
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 기간이_겹치는_Squad만_기수별_조회에_포함한다() {
        UmcProductGeneration generation = saveGeneration(3L);
        UmcProductSquad overlapping = squadAdapter.save(UmcProductSquad.create(
            "ONBOARDING",
            "온보딩 Squad",
            null,
            Instant.parse("2026-06-01T00:00:00Z"),
            Instant.parse("2026-07-01T00:00:00Z"),
            1,
            true
        ));
        squadAdapter.save(UmcProductSquad.create(
            "NO_PERIOD",
            "기간 없는 Squad",
            null,
            null,
            null,
            2,
            true
        ));
        squadAdapter.save(UmcProductSquad.create(
            "OLD",
            "과거 Squad",
            null,
            Instant.parse("2025-01-01T00:00:00Z"),
            Instant.parse("2025-02-01T00:00:00Z"),
            3,
            true
        ));

        assertThat(squadAdapter.listOverlapping(generation.getStartAt(), generation.getEndAt()))
            .extracting(UmcProductSquad::getId)
            .containsExactly(overlapping.getId());
    }

    private UmcProductGeneration saveGeneration(Long generation) {
        return generationAdapter.save(UmcProductGeneration.create(
            generation,
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-12-31T23:59:59Z"),
            false
        ));
    }

    private UmcProductFunctionalUnit saveFunctionalUnit(
        Long generationId,
        UmcProductFunctionalUnitType type,
        String code
    ) {
        return functionalUnitAdapter.save(UmcProductFunctionalUnit.create(
            generationId,
            null,
            type,
            code,
            code + " 이름",
            null,
            1,
            true
        ));
    }
}
