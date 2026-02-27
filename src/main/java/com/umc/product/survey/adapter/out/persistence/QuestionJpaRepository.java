package com.umc.product.survey.adapter.out.persistence;

import com.umc.product.survey.domain.Question;
import com.umc.product.survey.domain.enums.QuestionType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface QuestionJpaRepository extends JpaRepository<Question, Long> {
    @Modifying
    @Transactional
    @Query("""
                delete from Question q
                where q.id = :questionId
                  and q.formSection.id in (
                      select fs.id from FormSection fs
                      where fs.form.id = :formId
                  )
            """)
    int deleteByFormIdAndQuestionId(@Param("formId") Long formId,
                                    @Param("questionId") Long questionId);


    List<Question> findAllByFormSectionIdIn(Set<Long> formSectionIds);

    boolean existsByIdAndFormSection_Form_Id(Long questionId, Long formId);


    @Query("""
                select q
                from Question q
                join q.formSection fs
                join fs.form f
                where f.id = :formId
                  and q.type = :type
                order by q.id asc
            """)
    Optional<Question> findFirstByFormIdAndType(@Param("formId") Long formId, @Param("type") QuestionType type);

}
