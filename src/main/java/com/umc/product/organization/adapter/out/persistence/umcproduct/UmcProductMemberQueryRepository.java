package com.umc.product.organization.adapter.out.persistence.umcproduct;

import static com.umc.product.organization.domain.QUmcProductMember.umcProductMember;
import static com.umc.product.organization.domain.QUmcProductFunctionalMembership.umcProductFunctionalMembership;
import static com.umc.product.organization.domain.QUmcProductFunctionalUnit.umcProductFunctionalUnit;
import static com.umc.product.organization.domain.QUmcProductSquadParticipant.umcProductSquadParticipant;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductMemberSearchCondition;
import com.umc.product.organization.domain.UmcProductFunctionalMembership;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import com.umc.product.organization.domain.enums.UmcProductPosition;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UmcProductMemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Long> searchMemberIds(UmcProductMemberSearchCondition condition, Pageable pageable) {
        BooleanBuilder where = buildCondition(condition);
        boolean hasMembershipFilter = where.hasValue();
        boolean hasSquadFilter = condition != null && condition.squadId() != null;

        List<Long> content = hasMembershipFilter || hasSquadFilter
            ? queryFactory
                .select(umcProductMember.id)
                .distinct()
                .from(umcProductMember)
                .leftJoin(umcProductFunctionalMembership)
                .on(umcProductFunctionalMembership.umcProductMember.eq(umcProductMember))
                .leftJoin(umcProductFunctionalUnit)
                .on(umcProductFunctionalUnit.id.eq(umcProductFunctionalMembership.functionalUnitId))
                .leftJoin(umcProductSquadParticipant)
                .on(umcProductSquadParticipant.umcProductMember.eq(umcProductMember))
                .where(where)
                .orderBy(umcProductMember.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : queryFactory
                .select(umcProductMember.id)
                .from(umcProductMember)
                .orderBy(umcProductMember.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = hasMembershipFilter || hasSquadFilter
            ? queryFactory
                .select(umcProductMember.id.countDistinct())
                .from(umcProductMember)
                .leftJoin(umcProductFunctionalMembership)
                .on(umcProductFunctionalMembership.umcProductMember.eq(umcProductMember))
                .leftJoin(umcProductFunctionalUnit)
                .on(umcProductFunctionalUnit.id.eq(umcProductFunctionalMembership.functionalUnitId))
                .leftJoin(umcProductSquadParticipant)
                .on(umcProductSquadParticipant.umcProductMember.eq(umcProductMember))
                .where(where)
                .fetchOne()
            : queryFactory
                .select(umcProductMember.count())
                .from(umcProductMember)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    public List<UmcProductFunctionalMembership> listFunctionalMembershipsByMemberIds(
        Collection<Long> umcProductMemberIds
    ) {
        if (umcProductMemberIds == null || umcProductMemberIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(umcProductFunctionalMembership)
            .join(umcProductFunctionalMembership.umcProductMember).fetchJoin()
            .leftJoin(umcProductFunctionalUnit)
            .on(umcProductFunctionalUnit.id.eq(umcProductFunctionalMembership.functionalUnitId))
            .where(umcProductFunctionalMembership.umcProductMember.id.in(umcProductMemberIds))
            .orderBy(
                umcProductFunctionalMembership.umcProductGenerationId.desc(),
                umcProductFunctionalMembership.functionalUnitId.asc(),
                umcProductFunctionalMembership.role.desc(),
                umcProductFunctionalMembership.position.asc(),
                umcProductFunctionalMembership.id.asc()
            )
            .fetch();
    }

    private BooleanBuilder buildCondition(UmcProductMemberSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (condition == null) {
            return builder;
        }
        builder.and(umcProductGenerationIdEq(condition.umcProductGenerationId()));
        builder.and(functionalUnitIdEq(condition.functionalUnitId()));
        builder.and(functionalUnitTypeEq(condition.functionalUnitType()));
        builder.and(roleEq(condition.role()));
        builder.and(positionEq(condition.position()));
        builder.and(squadIdEq(condition.squadId()));
        return builder;
    }

    private BooleanExpression umcProductGenerationIdEq(Long umcProductGenerationId) {
        return umcProductGenerationId == null
            ? null
            : umcProductFunctionalMembership.umcProductGenerationId.eq(umcProductGenerationId);
    }

    private BooleanExpression functionalUnitIdEq(Long functionalUnitId) {
        return functionalUnitId == null ? null : umcProductFunctionalMembership.functionalUnitId.eq(functionalUnitId);
    }

    private BooleanExpression functionalUnitTypeEq(UmcProductFunctionalUnitType functionalUnitType) {
        return functionalUnitType == null ? null : umcProductFunctionalUnit.type.eq(functionalUnitType);
    }

    private BooleanExpression roleEq(UmcProductFunctionalRole role) {
        return role == null ? null : umcProductFunctionalMembership.role.eq(role);
    }

    private BooleanExpression positionEq(UmcProductPosition position) {
        return position == null ? null : umcProductFunctionalMembership.position.eq(position);
    }

    private BooleanExpression squadIdEq(Long squadId) {
        return squadId == null ? null : umcProductSquadParticipant.squad.id.eq(squadId);
    }
}
