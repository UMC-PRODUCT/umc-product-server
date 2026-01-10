package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.SingleAnswer;
import java.util.List;

public interface SaveSingleAnswerPort {

    void saveAll(List<SingleAnswer> answers);

    void upsertAll(Long formResponseId, List<SingleAnswer> answers);

}
