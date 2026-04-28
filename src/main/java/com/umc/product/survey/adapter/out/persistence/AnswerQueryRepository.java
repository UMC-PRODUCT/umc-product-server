package com.umc.product.survey.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.survey.domain.Answer;
import com.umc.product.survey.domain.QAnswer;
import com.umc.product.survey.domain.QFormSection;
import com.umc.product.survey.domain.QQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AnswerQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 특정 FormResponse 의 모든 답변을 조회.
     * 정렬: 섹션 orderNo asc -> 질문 orderNo asc (사용자가 본 화면 순서).
     */
    public List<Answer> findAllByFormResponseId(Long formResponseId) {
        QAnswer a = QAnswer.answer;
        QQuestion q = QQuestion.question;
        QFormSection s = QFormSection.formSection;
        return queryFactory
            .selectFrom(a)
            .join(a.question, q)
            .join(q.formSection, s)
            .where(a.formResponse.id.eq(formResponseId))
            .orderBy(s.orderNo.asc(), q.orderNo.asc())
            .fetch();
    }
}
