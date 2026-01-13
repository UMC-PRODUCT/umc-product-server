package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.Question;
import java.util.List;

public interface LoadQuestionPort {
    List<Question> findAllByFormId(Long formId);
}
