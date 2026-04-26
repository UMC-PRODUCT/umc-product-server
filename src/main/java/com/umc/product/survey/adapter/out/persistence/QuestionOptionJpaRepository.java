package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface QuestionOptionJpaRepository extends JpaRepository<QuestionOption, Long> {

    /**
     * 특정 질문에 속한 모든 선택지 삭제
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM QuestionOption o WHERE o.question.id = :questionId")
    int deleteAllByQuestionId(@Param("questionId") Long questionId);

    boolean existsByIdAndQuestion_Id(Long optionId, Long questionId);

    List<QuestionOption> findAllByQuestion_IdOrderByOrderNoAsc(Long questionId);
}
