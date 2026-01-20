package com.umc.product.survey.application.port.out;

import com.umc.product.survey.domain.Form;

public interface SaveFormPort {
    Form save(Form form);
}
