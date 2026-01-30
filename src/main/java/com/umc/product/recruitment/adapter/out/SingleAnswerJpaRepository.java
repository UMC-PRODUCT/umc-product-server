package com.umc.product.recruitment.adapter.out;

import com.umc.product.survey.domain.SingleAnswer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SingleAnswerJpaRepository extends JpaRepository<SingleAnswer, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from SingleAnswer a
                where a.formResponse.id in :formResponseIds
            """)
    int deleteAllByFormResponseIds(@Param("formResponseIds") List<Long> formResponseIds);
}
