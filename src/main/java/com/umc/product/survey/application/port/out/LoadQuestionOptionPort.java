package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.QuestionOption;
import java.util.List;
import java.util.Set;

public interface LoadQuestionOptionPort {
    List<QuestionOption> findAllByQuestionIdIn(Set<Long> questionIds);
}
