package com.umc.product.curriculum.adapter.out.persistence;

import static com.umc.product.challenger.domain.QChallenger.challenger;
import static com.umc.product.curriculum.domain.QChallengerWorkbook.challengerWorkbook;
import static com.umc.product.curriculum.domain.QOriginalWorkbook.originalWorkbook;
import static com.umc.product.member.domain.QMember.member;
import static com.umc.product.organization.domain.QSchool.school;
import static com.umc.product.organization.domain.QStudyGroupMember.studyGroupMember;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.curriculum.application.port.in.query.dto.GetWorkbookSubmissionsQuery;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookSubmissionInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WorkbookSubmissionQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 워크북 제출 현황 조회 (동적 필터링 + 커서 페이지네이션)
     */
    public List<WorkbookSubmissionInfo> findSubmissions(GetWorkbookSubmissionsQuery query) {
        var baseQuery = queryFactory
                .select(Projections.constructor(WorkbookSubmissionInfo.class,
                        challengerWorkbook.id,
                        challenger.id,
                        member.nickname,
                        member.profileImageId.stringValue(),
                        school.name,
                        challenger.part.stringValue(),
                        originalWorkbook.title,
                        challengerWorkbook.status
                ))
                .from(challengerWorkbook)
                .join(originalWorkbook).on(originalWorkbook.id.eq(challengerWorkbook.originalWorkbookId))
                .join(challenger).on(challenger.id.eq(challengerWorkbook.challengerId))
                .join(member).on(member.id.eq(challenger.memberId))
                .leftJoin(school).on(school.id.eq(member.schoolId));

        if (query.studyGroupId() != null) {
            baseQuery.join(studyGroupMember).on(studyGroupMember.challengerId.eq(challenger.id));
        }

        return baseQuery
                .where(
                        weekNoEq(query.weekNo()),
                        schoolIdEq(query.schoolId()),
                        studyGroupIdEq(query.studyGroupId()),
                        cursorGt(query.cursor())
                )
                .orderBy(challengerWorkbook.id.asc())
                .limit(query.fetchSize())
                .fetch();
    }

    private BooleanExpression weekNoEq(Integer weekNo) {
        return originalWorkbook.weekNo.eq(weekNo);
    }

    private BooleanExpression schoolIdEq(Long schoolId) {
        return schoolId != null ? member.schoolId.eq(schoolId) : null;
    }

    private BooleanExpression studyGroupIdEq(Long studyGroupId) {
        return studyGroupId != null ? studyGroupMember.studyGroup.id.eq(studyGroupId) : null;
    }

    private BooleanExpression cursorGt(Long cursor) {
        return cursor != null ? challengerWorkbook.id.gt(cursor) : null;
    }
}
