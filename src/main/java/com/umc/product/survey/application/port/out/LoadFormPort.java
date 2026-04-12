package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.Form;
import java.util.Optional;

public interface LoadFormPort {
    Optional<Form> findById(Long formId);
}
