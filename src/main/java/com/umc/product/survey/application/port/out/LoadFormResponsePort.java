package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.FormResponse;
import java.util.List;
import java.util.Optional;

public interface LoadFormResponsePort {

    Optional<FormResponse> findById(Long formResponseId);

    Optional<FormResponse> findDraftByFormIdAndRespondentMemberId(Long formId, Long respondentMemberId);

    List<FormResponse> findAllDraftByRespondentMemberId(Long respondentMemberId);
}
