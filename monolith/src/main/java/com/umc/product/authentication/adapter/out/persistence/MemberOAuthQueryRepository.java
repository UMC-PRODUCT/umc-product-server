package com.umc.product.authentication.adapter.out.persistence;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

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
