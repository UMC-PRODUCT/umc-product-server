package com.umc.product.form.application.port.out;

import com.umc.product.form.domain.Form;
import java.util.Optional;

public interface LoadFormPort {
    Optional<Form> findById(Long formId);
}
