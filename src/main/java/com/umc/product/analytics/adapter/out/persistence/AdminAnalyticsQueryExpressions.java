package com.umc.product.analytics.adapter.out.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.umc.product.challenger.domain.QChallengerPoint;
import com.umc.product.challenger.domain.enums.PointType;
import com.umc.product.organization.domain.QChapter;
import com.umc.product.organization.domain.QChapterSchool;

/**
 * Analytics 도메인의 QueryDSL 공통 표현식 모음.
 *
 * <p>모든 메서드는 stateless 라 인스턴스화 없이 호출한다.
 */
final class AdminAnalyticsQueryExpressions {

    private AdminAnalyticsQueryExpressions() {
    }

    /**
     * 한 ChallengerPoint 행의 실효 점수를 반환한다.
     *
     * <p>{@code pointValue} 컬럼이 비어 있지 않으면 그 값을, 그렇지 않으면 {@code PointType.value} 를 사용한다.
     * 도메인 엔티티의 {@link com.umc.product.challenger.domain.ChallengerPoint#getPointValue()} 와 동일한 의미를 SQL 로 표현한 것이다.
     */
    static NumberExpression<Double> pointScore(QChallengerPoint point) {
        CaseBuilder.Cases<Double, NumberExpression<Double>> typeCases = null;
        for (PointType pointType : PointType.values()) {
            if (typeCases == null) {
                typeCases = new CaseBuilder().when(point.type.eq(pointType)).then(pointType.getValue());
            } else {
                typeCases = typeCases.when(point.type.eq(pointType)).then(pointType.getValue());
            }
        }
        NumberExpression<Double> typeScore = typeCases != null ? typeCases.otherwise(0.0) : null;

        return new CaseBuilder()
            .when(point.pointValue.isNotNull()).then(point.pointValue.doubleValue())
            .otherwise(typeScore);
    }

    /**
     * {@link QChapterSchool} 가 LEFT JOIN 으로 다중-기수 행을 반환하는 문제를 막는 dedup 필터.
     *
     * <p>{@code ChapterSchool} 엔티티에는 gisu 컬럼이 없고 {@link QChapter} 만 gisu 를 가지므로,
     * 한 학교가 N 개 기수에 걸쳐 chapter_school 행을 갖는 경우 LEFT JOIN 이 N 개 행을 반환한다.
     * 그 결과 SUM/AVG/COUNT 집계가 N 배 부풀려진다.
     *
     * <p>이 필터는 chapter_school 매핑이 아예 없는 학교(orphan)는 통과시키되,
     * chapter_school 행이 존재하는 경우 현재 기수에 매칭된 chapter 가 있는 단 1개의 행만 통과시킨다.
     * 호출자는 반드시 LEFT JOIN {@code chapter} ON 절에 {@code chapter.gisu.id.eq(...)} 필터를 함께 둬야 한다.
     */
    static BooleanExpression chapterMatchedOrNoMapping(QChapterSchool chapterSchool, QChapter chapter) {
        return chapterSchool.id.isNull().or(chapter.id.isNotNull());
    }
}
