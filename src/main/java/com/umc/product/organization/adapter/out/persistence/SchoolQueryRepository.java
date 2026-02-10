package com.umc.product.organization.adapter.out.persistence;

import static com.umc.product.organization.domain.QChapter.chapter;
import static com.umc.product.organization.domain.QChapterSchool.chapterSchool;
import static com.umc.product.organization.domain.QGisu.gisu;
import static com.umc.product.organization.domain.QSchool.school;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class SchoolQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<SchoolListItemInfo> getSchools(SchoolSearchCondition condition, Pageable pageable) {
        // 활성 기수의 chapter id 서브쿼리
        JPQLQuery<Long> activeChapterIds = JPAExpressions
            .select(chapter.id)
            .from(chapter)
            .join(chapter.gisu, gisu)
            .where(gisu.isActive.isTrue());

        List<SchoolListItemInfo> content = queryFactory
            .select(Projections.constructor(SchoolListItemInfo.class,
                school.id,
                school.name,
                chapter.id,      // 활성 기수에 속하지 않으면 null
                chapter.name,    // 활성 기수에 속하지 않으면 null
                school.createdAt,
                chapter.id.isNotNull(),  // 활성 기수의 ChapterSchool 존재 여부
                school.remark,
                school.logoImageId       // 서비스 레이어에서 URL로 변환
            ))
            .from(school)
            .leftJoin(chapterSchool).on(
                chapterSchool.school.eq(school)
                    .and(chapterSchool.chapter.id.in(activeChapterIds))
            )
            .leftJoin(chapterSchool.chapter, chapter)
            .where(
                keywordContains(condition.keyword()),
                chapterIdEq(condition.chapterId())
            )
            .orderBy(school.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(school.count())
            .from(school)
            .leftJoin(chapterSchool).on(
                chapterSchool.school.eq(school)
                    .and(chapterSchool.chapter.id.in(activeChapterIds))
            )
            .leftJoin(chapterSchool.chapter, chapter)
            .where(
                keywordContains(condition.keyword()),
                chapterIdEq(condition.chapterId())
            );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? school.name.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression chapterIdEq(Long chapterId) {
        return chapterId != null ? chapter.id.eq(chapterId) : null;
    }

    /**
     * 학교 상세 정보를 반환합니다.
     */
    public SchoolDetailInfo.SchoolInfo getSchoolDetail(Long schoolId) {
        JPQLQuery<Long> activeChapterIds = JPAExpressions
            .select(chapter.id)
            .from(chapter)
            .join(chapter.gisu, gisu)
            .where(gisu.isActive.isTrue());

        return queryFactory
            .select(Projections.constructor(SchoolDetailInfo.SchoolInfo.class,
                chapter.id,
                chapter.name,
                school.name,
                school.id,
                school.remark,
                school.logoImageId,
                school.createdAt,
                school.updatedAt
            ))
            .from(school)
            .leftJoin(chapterSchool).on(
                chapterSchool.school.eq(school)
                    .and(chapterSchool.chapter.id.in(activeChapterIds))
            )
            .leftJoin(chapterSchool.chapter, chapter)
            .where(school.id.eq(schoolId))
            .fetchFirst();
        // TODO: fetchFirst로 임시로 해결은 해두었으나, 같은 기수 (활성 기수) 내에 학교가 중복된 지부에 속하는 경우 오류가 발생함.
        // 해당 경우를 고려해서 학교를 지부에 배정할 때 검증하는 로직을 추가할 필요성이 있음.
    }
}
