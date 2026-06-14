package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.UmcProductFunctionalUnit;

public interface SaveUmcProductFunctionalUnitPort {

    UmcProductFunctionalUnit save(UmcProductFunctionalUnit functionalUnit);

    void delete(UmcProductFunctionalUnit functionalUnit);
}
