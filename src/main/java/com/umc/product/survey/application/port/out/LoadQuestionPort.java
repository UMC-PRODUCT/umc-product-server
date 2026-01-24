package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.Question;
import java.util.List;
import java.util.Set;

public interface LoadQuestionPort {
    List<Question> findAllByFormSectionIdIn(Set<Long> formSectionIds);

    boolean existsByIdAndFormId(Long questionId, Long formId);

}
