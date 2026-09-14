package com.umc.product.authentication.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * MemberOAuth용 QueryDSL Repository
 */
@Repository
@RequiredArgsConstructor
public class MemberOAuthQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

//    public MemberOAuth findById(Long memberOAuthId) {
//        return jpaQueryFactory
//                .selectFrom(QMemberOAuth.memberOAuth)
//                .where(QMemberOAuth.memberOAuth.id.eq(memberOAuthId))
//                .fetchOne();
//    }
}
