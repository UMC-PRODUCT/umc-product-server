package com.umc.product.survey.adapter.out.persistence;

import static com.umc.product.survey.domain.QAnswer.answer;
import static com.umc.product.survey.domain.QAnswerChoice.answerChoice;
import static com.umc.product.survey.domain.QFormResponse.formResponse;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AnswerChoiceQueryRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 특정 폼의 SUBMITTED 응답 기준, 선택지별 득표수 집계.
     *
     * @return {optionId -> voteCount}. 득표 0인 선택지는 맵에 키가 없다.
     */
    public Map<Long, Long> countVotesByOptionId(Long formId) {
        List<OptionVoteCountRow> rows = queryFactory
            .select(Projections.constructor(OptionVoteCountRow.class,
                answerChoice.questionOption.id,
                answerChoice.id.count()))
            .from(answerChoice)
            .join(answerChoice.answer, answer)
            .join(answer.formResponse, formResponse)
            .where(
                formResponse.form.id.eq(formId),
                formResponse.status.eq(FormResponseStatus.SUBMITTED)
            )
            .groupBy(answerChoice.questionOption.id)
            .fetch();

        return rows.stream()
            .collect(Collectors.toMap(
                OptionVoteCountRow::optionId,
                OptionVoteCountRow::voteCount
            ));
    }

    /**
     * 특정 폼의 SUBMITTED 응답 기준, 선택지별 투표자 멤버 ID 조회.
     *
     * @return {optionId -> 투표자 memberId 리스트}. 투표자 없는 선택지는 맵에 키가 없다.
     */
    public Map<Long, List<Long>> findSelectedMemberIdsByOptionId(Long formId) {
        List<OptionMemberIdRow> rows = queryFactory
            .select(Projections.constructor(OptionMemberIdRow.class,
                answerChoice.questionOption.id,
                formResponse.respondentMemberId))
            .from(answerChoice)
            .join(answerChoice.answer, answer)
            .join(answer.formResponse, formResponse)
            .where(
                formResponse.form.id.eq(formId),
                formResponse.status.eq(FormResponseStatus.SUBMITTED)
            )
            .fetch();

        return rows.stream()
            .collect(Collectors.groupingBy(
                OptionMemberIdRow::optionId,
                Collectors.mapping(OptionMemberIdRow::memberId, Collectors.toList())
            ));
    }

    // ---------------------------------------------------------------------
    // Projection Records
    // ---------------------------------------------------------------------

    private record OptionVoteCountRow(Long optionId, Long voteCount) {}

    private record OptionMemberIdRow(Long optionId, Long memberId) {}
}
