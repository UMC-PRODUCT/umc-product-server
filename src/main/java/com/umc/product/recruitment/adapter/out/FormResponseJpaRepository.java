package com.umc.product.recruitment.adapter.out;

import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FormResponseJpaRepository extends JpaRepository<FormResponse, Long> {
    Optional<FormResponse> findFirstByForm_IdAndRespondentMemberIdAndStatusOrderByIdDesc(
            Long formId,
            Long respondentMemberId,
            FormResponseStatus status
    );

    List<FormResponse> findByRespondentMemberIdAndStatus(
            Long respondentMemberId,
            FormResponseStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
                delete from FormResponse fr
                where fr.form.id = :formId
                  and fr.status = :status
            """)
    int deleteByFormIdAndStatus(@Param("formId") Long formId,
                                @Param("status") FormResponseStatus status);

    @Query("""
                select fr.id
                from FormResponse fr
                where fr.form.id = :formId
                  and fr.status = :status
            """)
    List<Long> findIdsByFormIdAndStatus(@Param("formId") Long formId,
                                        @Param("status") FormResponseStatus status);
}
