package com.umc.product.organization.application.port.out;

import com.umc.product.organization.domain.Gisu;

public interface SaveGisuPort {

    Gisu save(Gisu gisu);

    void delete(Gisu gisu);
}
