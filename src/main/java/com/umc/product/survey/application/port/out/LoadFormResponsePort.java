package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.FormResponse;
import java.util.Optional;

public interface LoadFormResponsePort {

    Optional<FormResponse> findById(Long formResponseId);

    Optional<FormResponse> findDraftByFormIdAndMemberId(Long formId, Long memberId);
}
