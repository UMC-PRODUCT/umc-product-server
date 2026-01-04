package com.umc.product.form.application.port.out;

import com.umc.product.form.domain.FormResponse;

public interface SaveFormResponsePort {
    FormResponse save(FormResponse formResponse);
}
