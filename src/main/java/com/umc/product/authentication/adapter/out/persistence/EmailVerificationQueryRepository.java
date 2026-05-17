package com.umc.product.authentication.adapter.out.persistence;

import static com.umc.product.authentication.domain.QEmailVerification.emailVerification;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.authentication.domain.EmailVerification;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class EmailVerificationQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public Optional<EmailVerification> findById(Long id) {
        return Optional.ofNullable(
            jpaQueryFactory
                .selectFrom(emailVerification)
                .where(emailVerification.id.eq(id))
                .fetchOne()
        );
    }

    public Optional<EmailVerification> findByToken(String token) {
        return Optional.ofNullable(
            jpaQueryFactory
                .selectFrom(emailVerification)
                .where(emailVerification.token.eq(token))
                .fetchOne()
        );
    }

    public Optional<EmailVerification> findLatestSentByEmail(String email) {
        return Optional.ofNullable(
            jpaQueryFactory
                .selectFrom(emailVerification)
                .where(
                    emailVerification.email.eq(email),
                    emailVerification.lastSentAt.isNotNull()
                )
                .orderBy(emailVerification.lastSentAt.desc())
                .limit(1)
                .fetchOne()
        );
    }
}
