package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface QuestionOptionJpaRepository extends JpaRepository<QuestionOption, Long> {

    @Modifying
    @Transactional
    @Query("delete from QuestionOption o where o.question.id = :questionId")
    int deleteAllByQuestionId(@Param("questionId") Long questionId);
}
