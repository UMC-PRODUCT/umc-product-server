package com.umc.product.member.adapter.out.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.QChallenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.member.application.port.in.query.dto.SearchMemberQuery;
import com.umc.product.member.domain.Member;
import com.umc.product.member.domain.QMember;
import com.umc.product.organization.domain.QChapterSchool;
import com.umc.product.organization.domain.QSchool;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    /**
     * 검색 로직
     */
    public Page<Challenger> pagingSearch(SearchMemberQuery query, Pageable pageable) {
        QChallenger challenger = QChallenger.challenger;
        QMember member = QMember.member;

        BooleanBuilder condition = buildSearchCondition(query, challenger, member);

        List<Challenger> content = queryFactory
            .selectFrom(challenger)
            .join(member).on(challenger.memberId.eq(member.id))
            .where(condition)
            .orderBy(challenger.gisuId.desc(), member.schoolId.asc(), member.name.asc(), challenger.id.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(challenger.count())
            .from(challenger)
            .join(member).on(challenger.memberId.eq(member.id))
            .where(condition)
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }


    // ========= PRIVATE ============

    // 필터링
    private BooleanBuilder buildSearchCondition(SearchMemberQuery query, QChallenger challenger, QMember member) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(keywordContains(query.keyword(), member));
        builder.and(schoolIdEq(query.schoolId(), member));
        builder.and(chapterIdExists(query.chapterId(), member));
        builder.and(gisuIdEq(query.gisuId(), challenger));
        builder.and(partEq(query.part(), challenger));

        return builder;
    }

    // 검색 관련 조건
    private BooleanExpression keywordContains(String keyword, QMember member) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        QSchool school = QSchool.school;

        BooleanExpression schoolNameExists = JPAExpressions
            .selectOne()
            .from(school)
            .where(
                school.id.eq(member.schoolId),
                school.name.containsIgnoreCase(keyword)
            )
            .exists();

        return member.name.containsIgnoreCase(keyword)
            .or(member.email.containsIgnoreCase(keyword))
            .or(schoolNameExists);
    }

    private BooleanExpression chapterIdExists(Long chapterId, QMember member) {
        if (chapterId == null) {
            return null;
        }

        QChapterSchool chapterSchool = QChapterSchool.chapterSchool;

        return JPAExpressions
            .selectOne()
            .from(chapterSchool)
            .where(
                chapterSchool.school.id.eq(member.schoolId),
                chapterSchool.chapter.id.eq(chapterId)
            )
            .exists();
    }

    private BooleanExpression gisuIdEq(Long gisuId, QChallenger challenger) {
        return gisuId != null ? challenger.gisuId.eq(gisuId) : null;
    }

    private BooleanExpression partEq(ChallengerPart part, QChallenger challenger) {
        return part != null ? challenger.part.eq(part) : null;
    }

    private BooleanExpression schoolIdEq(Long schoolId, QMember member) {
        return schoolId != null ? member.schoolId.eq(schoolId) : null;
    }
}
