package com.umc.product.organization.adapter.out.persistence.productteam;

import static com.umc.product.organization.domain.QProductTeamMember.productTeamMember;
import static com.umc.product.organization.domain.QProductTeamMembership.productTeamMembership;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberSearchCondition;
import com.umc.product.organization.domain.ProductTeamMembership;
import com.umc.product.organization.domain.enums.ProductTeamPart;
import com.umc.product.organization.domain.enums.ProductTeamPosition;
import com.umc.product.organization.domain.enums.ProductTeamRole;
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

        List<Long> content = hasMembershipFilter
            ? queryFactory
                .select(productTeamMember.id)
                .distinct()
                .from(productTeamMember)
                .join(productTeamMembership).on(productTeamMembership.productTeamMember.eq(productTeamMember))
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

        Long total = hasMembershipFilter
            ? queryFactory
                .select(productTeamMember.id.countDistinct())
                .from(productTeamMember)
                .join(productTeamMembership).on(productTeamMembership.productTeamMember.eq(productTeamMember))
                .where(where)
                .fetchOne()
            : queryFactory
                .select(productTeamMember.count())
                .from(productTeamMember)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    public List<ProductTeamMembership> listMembershipsByMemberIds(
        Collection<Long> productTeamMemberIds,
        ProductTeamMemberSearchCondition condition
    ) {
        if (productTeamMemberIds == null || productTeamMemberIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
            .selectFrom(productTeamMembership)
            .join(productTeamMembership.productTeamMember).fetchJoin()
            .where(
                productTeamMembership.productTeamMember.id.in(productTeamMemberIds),
                productTeamGenerationIdEq(condition == null ? null : condition.productTeamGenerationId()),
                partEq(condition == null ? null : condition.part()),
                roleEq(condition == null ? null : condition.role()),
                positionEq(condition == null ? null : condition.position())
            )
            .orderBy(
                productTeamMembership.productTeamGenerationId.desc(),
                productTeamMembership.part.asc(),
                productTeamMembership.role.desc(),
                productTeamMembership.position.asc(),
                productTeamMembership.id.asc()
            )
            .fetch();
    }

    private BooleanBuilder buildCondition(ProductTeamMemberSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (condition == null) {
            return builder;
        }
        builder.and(productTeamGenerationIdEq(condition.productTeamGenerationId()));
        builder.and(partEq(condition.part()));
        builder.and(roleEq(condition.role()));
        builder.and(positionEq(condition.position()));
        return builder;
    }

    private BooleanExpression productTeamGenerationIdEq(Long productTeamGenerationId) {
        return productTeamGenerationId == null
            ? null
            : productTeamMembership.productTeamGenerationId.eq(productTeamGenerationId);
    }

    private BooleanExpression partEq(ProductTeamPart part) {
        return part == null ? null : productTeamMembership.part.eq(part);
    }

    private BooleanExpression roleEq(ProductTeamRole role) {
        return role == null ? null : productTeamMembership.role.eq(role);
    }

    private BooleanExpression positionEq(ProductTeamPosition position) {
        return position == null ? null : productTeamMembership.position.eq(position);
    }
}
