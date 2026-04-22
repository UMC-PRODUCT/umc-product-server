package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.LegacySingleAnswer;
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
}
