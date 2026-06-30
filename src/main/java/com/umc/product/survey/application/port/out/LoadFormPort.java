package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.Form;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoadFormPort {
    Optional<Form> findById(Long formId);

    List<Form> batchGetByIds(Collection<Long> formIds);
}
