package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerJpaRepository extends JpaRepository<Answer, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from Answer a
            where a.formResponse.id = :formResponseId
        """)
    int deleteAllByFormResponseId(@Param("formResponseId") Long formResponseId);
}
