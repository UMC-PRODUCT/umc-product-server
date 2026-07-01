package com.umc.product.curriculum.adapter.out.persistence;

import static com.umc.product.curriculum.domain.QCurriculum.curriculum;
import static com.umc.product.curriculum.domain.QWeeklyBestWorkbook.weeklyBestWorkbook;
import static com.umc.product.curriculum.domain.QWeeklyCurriculum.weeklyCurriculum;
import static com.umc.product.member.domain.QMember.member;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.dto.GetBestWorkbooksQuery;
import com.umc.product.curriculum.domain.WeeklyBestWorkbook;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class WeeklyBestWorkbookQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<WeeklyBestWorkbook> searchBestWorkbooks(GetBestWorkbooksQuery query) {
        return queryFactory
            .selectFrom(weeklyBestWorkbook)
            .join(weeklyBestWorkbook.weeklyCurriculum, weeklyCurriculum).fetchJoin()
            .join(weeklyCurriculum.curriculum, curriculum).fetchJoin()
            .leftJoin(member).on(member.id.eq(weeklyBestWorkbook.memberId))
            .where(
                gisuIdEq(query.gisuId()),
                schoolIdIn(query.schoolIds()),
                partIn(query.parts()),
                weekNoIn(query.weekNos()),
                studyGroupIdIn(query.studyGroupIds()),
                cursorLt(query.cursor())
            )
            .orderBy(weeklyBestWorkbook.id.desc())
            .limit(query.size())
            .fetch();
    }

    private BooleanExpression gisuIdEq(Long gisuId) {
        return gisuId != null ? curriculum.gisuId.eq(gisuId) : null;
    }

    private BooleanExpression schoolIdIn(Set<Long> schoolIds) {
        return hasValues(schoolIds) ? member.schoolId.in(schoolIds) : null;
    }

    private BooleanExpression partIn(Set<ChallengerPart> parts) {
        return hasValues(parts) ? curriculum.part.in(parts) : null;
    }

    private BooleanExpression weekNoIn(List<Long> weekNos) {
        return hasValues(weekNos) ? weeklyCurriculum.weekNo.in(weekNos) : null;
    }

    private BooleanExpression studyGroupIdIn(List<Long> studyGroupIds) {
        return hasValues(studyGroupIds) ? weeklyBestWorkbook.studyGroupId.in(studyGroupIds) : null;
    }

    private BooleanExpression cursorLt(Long cursor) {
        return cursor != null ? weeklyBestWorkbook.id.lt(cursor) : null;
    }

    private boolean hasValues(Collection<?> values) {
        return values != null && !values.isEmpty();
    }
}
