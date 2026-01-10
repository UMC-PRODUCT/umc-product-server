package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.SingleAnswer;
import java.util.List;

public interface LoadSingleAnswerPort {
    List<SingleAnswer> findByFormResponseId(Long formResponseId);
}
