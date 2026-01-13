package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.FormResponse;
import java.util.Optional;

public interface LoadFormResponsePort {

    boolean existsByFormIdAndUserId(Long formId, Long userId);

    Optional<FormResponse> findByFormIdAndUserId(Long formId, Long userId);

    Optional<FormResponse> findDraftByFormIdAndUserId(Long formId, Long userId);
}
