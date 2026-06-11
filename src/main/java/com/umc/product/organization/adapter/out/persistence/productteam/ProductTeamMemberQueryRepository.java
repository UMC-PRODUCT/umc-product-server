package com.umc.product.organization.adapter.out.persistence.productteam;

import static com.umc.product.organization.domain.QProductTeamMember.productTeamMember;
import static com.umc.product.organization.domain.QProductTeamFunctionalMembership.productTeamFunctionalMembership;
import static com.umc.product.organization.domain.QProductTeamFunctionalUnit.productTeamFunctionalUnit;
import static com.umc.product.organization.domain.QProductTeamSquadParticipant.productTeamSquadParticipant;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberSearchCondition;
import com.umc.product.organization.domain.ProductTeamFunctionalMembership;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductTeamMemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Long> searchMemberIds(ProductTeamMemberSearchCondition condition, Pageable pageable) {
        BooleanBuilder where = buildCondition(condition);
        boolean hasMembershipFilter = where.hasValue();
        boolean hasSquadFilter = condition != null && condition.squadId() != null;

        List<Long> content = hasMembershipFilter || hasSquadFilter
            ? queryFactory
                .select(productTeamMember.id)
                .distinct()
                .from(productTeamMember)
                .leftJoin(productTeamFunctionalMembership)
                .on(productTeamFunctionalMembership.productTeamMember.eq(productTeamMember))
                .leftJoin(productTeamFunctionalUnit)
                .on(productTeamFunctionalUnit.id.eq(productTeamFunctionalMembership.functionalUnitId))
                .leftJoin(productTeamSquadParticipant)
                .on(productTeamSquadParticipant.productTeamMember.eq(productTeamMember))
                .where(where)
                .orderBy(productTeamMember.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : queryFactory
                .select(productTeamMember.id)
                .from(productTeamMember)
                .orderBy(productTeamMember.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = hasMembershipFilter || hasSquadFilter
            ? queryFactory
                .select(productTeamMember.id.countDistinct())
                .from(productTeamMember)
                .leftJoin(productTeamFunctionalMembership)
                .on(productTeamFunctionalMembership.productTeamMember.eq(productTeamMember))
                .leftJoin(productTeamFunctionalUnit)
                .on(productTeamFunctionalUnit.id.eq(productTeamFunctionalMembership.functionalUnitId))
                .leftJoin(productTeamSquadParticipant)
                .on(productTeamSquadParticipant.productTeamMember.eq(productTeamMember))
                .where(where)
                .fetchOne()
            : queryFactory
                .select(productTeamMember.count())
                .from(productTeamMember)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    public List<ProductTeamFunctionalMembership> listFunctionalMembershipsByMemberIds(
        Collection<Long> productTeamMemberIds,
        ProductTeamMemberSearchCondition condition
    ) {
        if (productTeamMemberIds == null || productTeamMemberIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productTeamFunctionalMembership)
            .join(productTeamFunctionalMembership.productTeamMember).fetchJoin()
            .leftJoin(productTeamFunctionalUnit)
            .on(productTeamFunctionalUnit.id.eq(productTeamFunctionalMembership.functionalUnitId))
            .where(
                productTeamFunctionalMembership.productTeamMember.id.in(productTeamMemberIds),
                productTeamGenerationIdEq(condition == null ? null : condition.productTeamGenerationId()),
                functionalUnitIdEq(condition == null ? null : condition.functionalUnitId()),
                functionalUnitTypeEq(condition == null ? null : condition.functionalUnitType()),
                roleEq(condition == null ? null : condition.role()),
                positionEq(condition == null ? null : condition.position())
            )
            .orderBy(
                productTeamFunctionalMembership.productTeamGenerationId.desc(),
                productTeamFunctionalMembership.functionalUnitId.asc(),
                productTeamFunctionalMembership.role.desc(),
                productTeamFunctionalMembership.position.asc(),
                productTeamFunctionalMembership.id.asc()
            )
            .fetch();
    }

    private BooleanBuilder buildCondition(ProductTeamMemberSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (condition == null) {
            return builder;
        }
        builder.and(productTeamGenerationIdEq(condition.productTeamGenerationId()));
        builder.and(functionalUnitIdEq(condition.functionalUnitId()));
        builder.and(functionalUnitTypeEq(condition.functionalUnitType()));
        builder.and(roleEq(condition.role()));
        builder.and(positionEq(condition.position()));
        builder.and(squadIdEq(condition.squadId()));
        return builder;
    }

    private BooleanExpression productTeamGenerationIdEq(Long productTeamGenerationId) {
        return productTeamGenerationId == null
            ? null
            : productTeamFunctionalMembership.productTeamGenerationId.eq(productTeamGenerationId);
    }

    private BooleanExpression functionalUnitIdEq(Long functionalUnitId) {
        return functionalUnitId == null ? null : productTeamFunctionalMembership.functionalUnitId.eq(functionalUnitId);
    }

    private BooleanExpression functionalUnitTypeEq(ProductTeamFunctionalUnitType functionalUnitType) {
        return functionalUnitType == null ? null : productTeamFunctionalUnit.type.eq(functionalUnitType);
    }

    private BooleanExpression roleEq(ProductTeamFunctionalRole role) {
        return role == null ? null : productTeamFunctionalMembership.role.eq(role);
    }

    private BooleanExpression positionEq(ProductTeamPosition position) {
        return position == null ? null : productTeamFunctionalMembership.position.eq(position);
    }

    private BooleanExpression squadIdEq(Long squadId) {
        return squadId == null ? null : productTeamSquadParticipant.squad.id.eq(squadId);
    }
}
