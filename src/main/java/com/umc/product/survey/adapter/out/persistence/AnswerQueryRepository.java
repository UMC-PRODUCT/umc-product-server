package com.umc.product.survey.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.survey.domain.Answer;
import com.umc.product.survey.domain.QAnswer;
import com.umc.product.survey.domain.QFormResponse;
import com.umc.product.survey.domain.QFormSection;
import com.umc.product.survey.domain.QQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

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

    /**
     * 여러 FormResponse 의 모든 답변을 한 번에 조회한다.
     */
    public List<Answer> findAllByFormResponseIdIn(Set<Long> formResponseIds) {
        if (formResponseIds == null || formResponseIds.isEmpty()) {
            return List.of();
        }

        QAnswer a = QAnswer.answer;
        QQuestion q = QQuestion.question;
        QFormSection s = QFormSection.formSection;
        QFormResponse fr = QFormResponse.formResponse;
        return queryFactory
            .selectFrom(a)
            .join(a.formResponse, fr).fetchJoin()
            .join(a.question, q).fetchJoin()
            .join(q.formSection, s)
            .where(a.formResponse.id.in(formResponseIds))
            .orderBy(a.formResponse.id.asc(), s.orderNo.asc(), q.orderNo.asc())
            .fetch();
    }

    /**
     * 특정 FormResponse 에 특정 Question의 답변이 이미 있는지 검사 (createAnswer 중복 검증 용).
     */
    public boolean existsByFormResponseIdAndQuestionId(Long formResponseId, Long questionId) {
        QAnswer a = QAnswer.answer;
        Integer one = queryFactory
            .selectOne()
            .from(a)
            .where(
                a.formResponse.id.eq(formResponseId),
                a.question.id.eq(questionId)
            )
            .fetchFirst();
        return one != null;
    }
}
