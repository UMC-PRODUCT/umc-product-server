package com.umc.product.form.application.port.out;

import com.umc.product.form.domain.SingleAnswer;
import java.util.List;

public interface LoadSingleAnswerPort {
    List<SingleAnswer> findByFormResponseId(Long formResponseId);
}
