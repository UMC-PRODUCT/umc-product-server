package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.LegacySingleAnswer;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Deprecated
public interface LegacySingleAnswerJpaRepository extends JpaRepository<LegacySingleAnswer, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from LegacySingleAnswer a
            where a.formResponse.id in :formResponseIds
        """)
    int deleteAllByFormResponseIds(@Param("formResponseIds") List<Long> formResponseIds);


    @Query("""
            select sa
            from LegacySingleAnswer sa
            join sa.formResponse fr
            where fr.form.id = :formId
              and fr.respondentMemberId = :memberId
              and fr.status = :status
            order by fr.id desc, sa.id desc
        """)
    List<LegacySingleAnswer> findLatestSubmittedAnswers(
        @Param("formId") Long formId,
        @Param("memberId") Long memberId,
        @Param("status") FormResponseStatus status
    );

    @Query(value = """
        select t.option_id as optionId, sum(t.cnt) as cnt
        from (
            select (sa.value->>'selectedOptionId')::bigint as option_id, count(*) as cnt
            from single_answer sa
            join form_response fr on fr.id = sa.response_id
            where fr.form_id = :formId
              and fr.status = 'SUBMITTED'
              and sa.answered_as_type in ('RADIO','DROPDOWN')
              and jsonb_exists(sa.value, 'selectedOptionId')
            group by (sa.value->>'selectedOptionId')::bigint

            union all

            select (elem)::bigint as option_id, count(*) as cnt
            from single_answer sa
            join form_response fr on fr.id = sa.response_id
            cross join lateral jsonb_array_elements_text(sa.value->'selectedOptionIds') as elem
            where fr.form_id = :formId
              and fr.status = 'SUBMITTED'
              and sa.answered_as_type = 'CHECKBOX'
              and jsonb_exists(sa.value, 'selectedOptionIds')
            group by (elem)::bigint
        ) t
        group by t.option_id
        """, nativeQuery = true)
    List<Object[]> countVotesByOptionId(@Param("formId") Long formId);

}
