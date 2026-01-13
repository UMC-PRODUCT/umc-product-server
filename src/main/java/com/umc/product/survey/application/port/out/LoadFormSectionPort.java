package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.FormSection;
import java.util.List;

public interface LoadFormSectionPort {
    List<FormSection> findAllByFormId(Long formId);
}
