package com.umc.product.authentication.adapter.out.persistence;

import static com.umc.product.authentication.domain.QEmailVerification.emailVerification;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.authentication.domain.EmailVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class EmailVerificationQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    EmailVerification findById(Long id) {
        return jpaQueryFactory
            .selectFrom(emailVerification)
            .where(emailVerification.id.eq(id))
            .fetchOne();
    }

    EmailVerification findByToken(String token) {
        return jpaQueryFactory
            .selectFrom(emailVerification)
            .where(emailVerification.token.eq(token))
            .fetchOne();
    }
}
