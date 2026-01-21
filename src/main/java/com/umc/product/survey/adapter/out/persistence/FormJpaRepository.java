package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.Form;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FormJpaRepository extends JpaRepository<Form, Long> {
    @Query("""
                select distinct f
                from Form f
                left join fetch f.sections s
                left join fetch s.questions q
                left join fetch q.options o
                where f.id = :formId
            """)
    Optional<Form> findDefinitionById(@Param("formId") Long formId);
}
