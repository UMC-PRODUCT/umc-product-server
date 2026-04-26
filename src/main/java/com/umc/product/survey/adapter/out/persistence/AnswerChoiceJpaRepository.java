package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.adapter.out.persistence.projection.OptionMemberIdProjection;
import com.umc.product.survey.adapter.out.persistence.projection.OptionVoteCountProjection;
import com.umc.product.survey.domain.AnswerChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerChoiceJpaRepository extends JpaRepository<AnswerChoice, Long> {

    /**
     * 특정 폼의 SUBMITTED 응답 기준, 선택지별 득표수 집계
     */
    @Query("""
            SELECT new com.umc.product.survey.adapter.out.persistence.projection.OptionVoteCountProjection(
                ac.questionOption.id, count(ac.id)
            )
            FROM AnswerChoice ac
            JOIN ac.answer a
            JOIN a.formResponse fr
            WHERE fr.form.id = :formId AND fr.status = 'SUBMITTED'
            GROUP BY ac.questionOption.id
        """)
    List<OptionVoteCountProjection> countVotesByOptionId(@Param("formId") Long formId);

    /**
     * 특정 폼에서 특정 멤버가 선택한 선택지 ID 목록 조회 (SUBMITTED 응답 기준)
     */
    @Query("""
            SELECT ac.questionOption.id
            FROM AnswerChoice ac
            JOIN ac.answer a
            JOIN a.formResponse fr
            WHERE fr.form.id = :formId
              AND fr.respondentMemberId = :memberId
              AND fr.status = 'SUBMITTED'
        """)
    List<Long> findSelectedOptionIdsByMember(@Param("formId") Long formId, @Param("memberId") Long memberId);

    /**
     * 특정 폼의 SUBMITTED 응답 기준, 선택지별 투표자 멤버 ID 조회
     */
    @Query("""
            SELECT new com.umc.product.survey.adapter.out.persistence.projection.OptionMemberIdProjection(
                ac.questionOption.id, fr.respondentMemberId
            )
            FROM AnswerChoice ac
            JOIN ac.answer a
            JOIN a.formResponse fr
            WHERE fr.form.id = :formId AND fr.status = 'SUBMITTED'
        """)
    List<OptionMemberIdProjection> findSelectedMemberIdsByOptionId(@Param("formId") Long formId);
}
