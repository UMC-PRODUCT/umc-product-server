package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.Evaluation;

public interface SaveEvaluationPort {

    Evaluation save(Evaluation evaluation);
}
