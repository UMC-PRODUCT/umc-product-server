package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.FormSection;

public interface SaveFormSectionPort {

    FormSection save(FormSection formSection);
}
