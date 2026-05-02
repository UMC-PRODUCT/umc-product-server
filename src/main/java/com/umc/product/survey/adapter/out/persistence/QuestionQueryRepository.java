package com.umc.product.survey.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.survey.domain.QQuestion;
import com.umc.product.survey.domain.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class QuestionQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 섹션에 속한 모든 질문을 orderNo 오름차순으로 조회.
     */
    public List<Question> findAllBySectionId(Long sectionId) {
        QQuestion q = QQuestion.question;
        return queryFactory
            .selectFrom(q)
            .where(q.formSection.id.eq(sectionId))
            .orderBy(q.orderNo.asc())
            .fetch();
    }

    /**
     * 여러 섹션의 모든 질문을 벌크 조회. orderNo 오름차순 정렬.
     */
    public List<Question> findAllBySectionIdIn(Set<Long> sectionIds) {
        if (sectionIds.isEmpty()) {
            return List.of();
        }
        QQuestion q = QQuestion.question;
        return queryFactory
            .selectFrom(q)
            .where(q.formSection.id.in(sectionIds))
            .orderBy(q.orderNo.asc())
            .fetch();
    }
}
