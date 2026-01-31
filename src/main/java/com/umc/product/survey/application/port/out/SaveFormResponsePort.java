package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.FormResponse;
import java.util.List;

public interface SaveFormResponsePort {
    FormResponse save(FormResponse formResponse);

    void deleteById(Long formResponseId);

    void deleteDraftsByFormId(Long formId);

    void deleteAllByIds(List<Long> ids);
}
