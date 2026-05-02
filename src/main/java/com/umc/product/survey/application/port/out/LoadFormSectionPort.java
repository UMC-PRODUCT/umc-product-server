package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.FormSection;
import java.util.List;
import java.util.Optional;

public interface LoadFormSectionPort {

    Optional<FormSection> findById(Long sectionId);

    List<FormSection> listByFormId(Long formId);
}
