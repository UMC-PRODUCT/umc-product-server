package com.umc.product.form.application.port.out;

import com.umc.product.form.domain.Question;
import java.util.List;

public interface LoadQuestionPort {
    List<Question> findAllByFormId(Long formId);
}
