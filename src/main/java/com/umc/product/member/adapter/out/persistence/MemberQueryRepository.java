package com.umc.product.member.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.QMember;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Long countAllMembers() {
        return queryFactory
                .selectFrom(QMember.member)
                .stream().count();
    }

    public Optional<Member> findByNickname(String nickname) {
        return Optional.ofNullable(queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.nickname.eq(nickname))
                .fetchFirst()
        );
    }
}
