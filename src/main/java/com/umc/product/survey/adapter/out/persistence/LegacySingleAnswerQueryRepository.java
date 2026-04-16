package com.umc.product.survey.adapter.out.persistence;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.survey.application.port.out.dto.VoteAnswerRow;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import com.umc.product.survey.domain.enums.QuestionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.umc.product.survey.domain.QFormResponse.formResponse;
import static com.umc.product.survey.domain.QLegacySingleAnswer.legacySingleAnswer;
import static com.umc.product.survey.domain.QQuestion.question;

@Repository
@RequiredArgsConstructor
@Deprecated
public class LegacySingleAnswerQueryRepository {


    private final JPAQueryFactory queryFactory;

    public Map<Long, Map<String, Object>> findScheduleValuesByFormResponseIds(List<Long> formResponseIds) {
        if (formResponseIds == null || formResponseIds.isEmpty()) {
            return Map.of();
        }

        List<Tuple> rows = queryFactory
            .select(legacySingleAnswer.formResponse.id, legacySingleAnswer.value)
            .from(legacySingleAnswer)
            .join(legacySingleAnswer.question, question)
            .where(
                legacySingleAnswer.formResponse.id.in(formResponseIds),
                question.type.eq(QuestionType.SCHEDULE)
            )
            .fetch();

        Map<Long, Map<String, Object>> result = new HashMap<>();
        for (Tuple row : rows) {
            Long formResponseId = row.get(legacySingleAnswer.formResponse.id);
            @SuppressWarnings("unchecked")
            Map<String, Object> value = row.get(legacySingleAnswer.value);

            // formResponse당 schedule 답변 1개 가정 (여러개면 마지막)
            if (formResponseId != null && value != null) {
                result.put(formResponseId, value);
            }
        }
        return result;
    }

    public List<VoteAnswerRow> findVoteAnswerRows(Long voteId) {
        return queryFactory
            .select(Projections.constructor(
                VoteAnswerRow.class,
                formResponse.respondentMemberId,
                legacySingleAnswer.answeredAsType,
                legacySingleAnswer.value
            ))
            .from(legacySingleAnswer)
            .join(legacySingleAnswer.formResponse, formResponse)
            .where(
                formResponse.form.id.eq(voteId),
                formResponse.status.eq(FormResponseStatus.SUBMITTED)
            )
            .fetch();
    }
}
