package com.umc.product.form.application.port.out;

import com.umc.product.form.domain.QuestionOption;
import java.util.List;
import java.util.Set;

public interface LoadQuestionOptionPort {
    List<QuestionOption> findAllByQuestionIdIn(Set<Long> questionIds);
}
