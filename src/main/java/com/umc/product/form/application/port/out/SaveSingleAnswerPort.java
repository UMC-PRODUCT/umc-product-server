package com.umc.product.form.application.port.out;

import com.umc.product.form.domain.SingleAnswer;
import java.util.List;

public interface SaveSingleAnswerPort {
    void saveAll(List<SingleAnswer> answers);
}
