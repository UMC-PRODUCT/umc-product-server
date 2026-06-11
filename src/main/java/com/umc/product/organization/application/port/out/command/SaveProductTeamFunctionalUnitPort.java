package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.ProductTeamFunctionalUnit;

public interface SaveProductTeamFunctionalUnitPort {

    ProductTeamFunctionalUnit save(ProductTeamFunctionalUnit functionalUnit);

    void delete(ProductTeamFunctionalUnit functionalUnit);
}
