package com.umc.product.survey.adapter.out.persistence;

import static com.umc.product.survey.domain.QQuestion.question;
import static com.umc.product.survey.domain.QSingleAnswer.singleAnswer;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.survey.domain.enums.QuestionType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SingleAnswerQueryRepository {


    private final JPAQueryFactory queryFactory;

    public Map<Long, Map<String, Object>> findScheduleValuesByFormResponseIds(List<Long> formResponseIds) {
        if (formResponseIds == null || formResponseIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> rows = queryFactory
            .select(singleAnswer.formResponse.id, singleAnswer.value)
            .from(singleAnswer)
            .join(singleAnswer.question, question)
            .where(
                singleAnswer.formResponse.id.in(formResponseIds),
                question.type.eq(QuestionType.SCHEDULE)
            )
            .fetch();

        Map<Long, Map<String, Object>> result = new HashMap<>();
        for (Tuple row : rows) {
            Long formResponseId = row.get(singleAnswer.formResponse.id);
            @SuppressWarnings("unchecked")
            Map<String, Object> value = row.get(singleAnswer.value);

            // formResponse당 schedule 답변 1개 가정 (여러개면 마지막)
            if (formResponseId != null && value != null) {
                result.put(formResponseId, value);
            }
        }
        return result;
    }
}
