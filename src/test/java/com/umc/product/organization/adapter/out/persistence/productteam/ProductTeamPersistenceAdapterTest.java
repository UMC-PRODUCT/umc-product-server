package com.umc.product.organization.adapter.out.persistence.productteam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.global.config.JpaConfig;
import com.umc.product.global.config.QueryDslConfig;
import com.umc.product.organization.domain.ProductTeamGeneration;
import com.umc.product.organization.domain.ProductTeamMember;
import com.umc.product.organization.domain.ProductTeamMembership;
import com.umc.product.organization.domain.enums.ProductTeamPart;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamRole;
import com.umc.product.support.TestContainersConfig;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
    JpaConfig.class,
    QueryDslConfig.class,
    TestContainersConfig.class,
    ProductTeamMemberQueryRepository.class,
    ProductTeamGenerationPersistenceAdapter.class,
    ProductTeamMemberPersistenceAdapter.class,
    ProductTeamMembershipPersistenceAdapter.class
})
class ProductTeamPersistenceAdapterTest {

    @Autowired
    TestEntityManager em;

    @Autowired
    ProductTeamGenerationPersistenceAdapter generationAdapter;

    @Autowired
    ProductTeamMemberPersistenceAdapter memberAdapter;

    @Autowired
    ProductTeamMembershipPersistenceAdapter membershipAdapter;

    @Test
    void 같은_기수에서_여러_직책과_포지션을_겸임할_수_있다() {
        ProductTeamGeneration generation = saveGeneration(1L);
        ProductTeamMember member = memberAdapter.save(ProductTeamMember.create(100L, "소개", null));

        membershipAdapter.saveAll(List.of(
            ProductTeamMembership.create(
                member,
                generation.getId(),
                ProductTeamPart.SERVER,
                ProductTeamRole.TEAM_LEADER,
                ProductTeamPosition.BACKEND_DEVELOPER
            ),
            ProductTeamMembership.create(
                member,
                generation.getId(),
                ProductTeamPart.MOBILE,
                ProductTeamRole.MEMBER,
                ProductTeamPosition.ANDROID_DEVELOPER
            )
        ));

        em.flush();
        em.clear();

        assertThat(membershipAdapter.listByProductTeamMemberId(member.getId()))
            .extracting(ProductTeamMembership::getPosition)
            .containsExactlyInAnyOrder(ProductTeamPosition.BACKEND_DEVELOPER, ProductTeamPosition.ANDROID_DEVELOPER);
    }

    @Test
    void 같은_활동_기록은_중복_저장할_수_없다() {
        ProductTeamGeneration generation = saveGeneration(2L);
        ProductTeamMember member = memberAdapter.save(ProductTeamMember.create(101L, "소개", null));

        membershipAdapter.save(ProductTeamMembership.create(
            member,
            generation.getId(),
            ProductTeamPart.MOBILE,
            ProductTeamRole.MEMBER,
            ProductTeamPosition.IOS_DEVELOPER
        ));
        assertThatThrownBy(() -> {
            membershipAdapter.save(ProductTeamMembership.create(
                member,
                generation.getId(),
                ProductTeamPart.MOBILE,
                ProductTeamRole.MEMBER,
                ProductTeamPosition.IOS_DEVELOPER
            ));
            em.flush();
        })
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 같은_기수와_파트에는_TEAM_LEADER를_한_명만_둘_수_있다() {
        ProductTeamGeneration generation = saveGeneration(3L);
        ProductTeamMember first = memberAdapter.save(ProductTeamMember.create(102L, "소개", null));
        ProductTeamMember second = memberAdapter.save(ProductTeamMember.create(103L, "소개", null));

        membershipAdapter.save(ProductTeamMembership.create(
            first,
            generation.getId(),
            ProductTeamPart.DESIGN,
            ProductTeamRole.TEAM_LEADER,
            ProductTeamPosition.PRODUCT_DESIGNER
        ));
        assertThatThrownBy(() -> {
            membershipAdapter.save(ProductTeamMembership.create(
                second,
                generation.getId(),
                ProductTeamPart.DESIGN,
                ProductTeamRole.TEAM_LEADER,
                ProductTeamPosition.PRODUCT_DESIGNER
            ));
            em.flush();
        })
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    private ProductTeamGeneration saveGeneration(Long generation) {
        return generationAdapter.save(ProductTeamGeneration.create(
            generation,
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-12-31T23:59:59Z"),
            false
        ));
    }
}
