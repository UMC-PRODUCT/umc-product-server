package com.umc.product.audit.adapter.out.persistence;

import static com.umc.product.audit.domain.QAuditLog.auditLog;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.audit.domain.AuditLog;
import com.umc.product.global.exception.constant.Domain;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditLogQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<AuditLog> search(
        Domain domain, AuditAction action, Long actorMemberId,
        Instant from, Instant to, Pageable pageable
    ) {
        BooleanBuilder condition = new BooleanBuilder();

        if (domain != null) {
            condition.and(auditLog.domain.eq(domain));
        }
        if (action != null) {
            condition.and(auditLog.action.eq(action));
        }
        if (actorMemberId != null) {
            condition.and(auditLog.actorMemberId.eq(actorMemberId));
        }
        if (from != null) {
            condition.and(auditLog.createdAt.goe(from));
        }
        if (to != null) {
            condition.and(auditLog.createdAt.loe(to));
        }

        List<AuditLog> content = queryFactory
            .selectFrom(auditLog)
            .where(condition)
            .orderBy(auditLog.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(auditLog.count())
            .from(auditLog)
            .where(condition)
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
