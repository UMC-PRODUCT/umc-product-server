package com.umc.product.survey.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.survey.domain.QQuestionOption;
import com.umc.product.survey.domain.QuestionOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class QuestionOptionQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 질문에 속한 모든 선택지를 orderNo 오름차순으로 조회.
     */
    public List<QuestionOption> findAllByQuestionId(Long questionId) {
        QQuestionOption o = QQuestionOption.questionOption;
        return queryFactory
            .selectFrom(o)
            .where(o.question.id.eq(questionId))
            .orderBy(o.orderNo.asc())
            .fetch();
    }
}
