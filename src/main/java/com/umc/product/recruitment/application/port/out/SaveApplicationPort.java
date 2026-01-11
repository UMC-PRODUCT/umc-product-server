package com.umc.product.recruitment.application.port.out;

import com.umc.product.recruitment.domain.Application;

public interface SaveApplicationPort {
    Application save(Application application);
}
