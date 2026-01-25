package com.umc.product.recruitment.adapter.out;

import com.umc.product.survey.domain.FormResponse;
import com.umc.product.survey.domain.enums.FormResponseStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
