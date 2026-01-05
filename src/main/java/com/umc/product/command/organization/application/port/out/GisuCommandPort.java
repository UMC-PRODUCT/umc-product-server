package com.umc.product.command.organization.application.port.out;

import com.umc.product.command.organization.domain.Gisu;

public interface GisuCommandPort {

    Gisu save(Gisu gisu);

    void delete(Gisu gisu);
}
