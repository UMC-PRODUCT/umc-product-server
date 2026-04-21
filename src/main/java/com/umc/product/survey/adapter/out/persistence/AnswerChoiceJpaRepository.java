package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.AnswerChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerChoiceJpaRepository extends JpaRepository<AnswerChoice, Long> {

    @Query("""
            select ac.questionOption.id as optionId, count(ac.id) as voteCount
            from AnswerChoice ac
            join ac.answer a
            join a.formResponse fr
            where fr.form.id = :formId and fr.status = 'SUBMITTED'
            group by ac.questionOption.id
        """)
    List<Object[]> countVotesByOptionIdRaw(@Param("formId") Long formId);

    @Query("""
            select ac.questionOption.id
            from AnswerChoice ac
            join ac.answer a
            join a.formResponse fr
            where fr.form.id = :formId 
              and fr.respondentMemberId = :memberId
              and fr.status = 'SUBMITTED'
        """)
    List<Long> findSelectedOptionIdsByMember(@Param("formId") Long formId, @Param("memberId") Long memberId);

    @Query("""
            select ac.questionOption.id, fr.respondentMemberId
            from AnswerChoice ac
            join ac.answer a
            join a.formResponse fr
            where fr.form.id = :formId and fr.status = 'SUBMITTED'
        """)
    List<Object[]> findSelectedMemberIdsByOptionIdRaw(@Param("formId") Long formId);
}
