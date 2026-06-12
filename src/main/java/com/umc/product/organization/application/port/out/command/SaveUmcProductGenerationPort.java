package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.UmcProductGeneration;

public interface SaveUmcProductGenerationPort {

    UmcProductGeneration save(UmcProductGeneration generation);

    void delete(UmcProductGeneration generation);
}
