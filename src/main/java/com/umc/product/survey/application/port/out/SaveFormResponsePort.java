package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.FormResponse;

public interface SaveFormResponsePort {
    FormResponse save(FormResponse formResponse);
}
