package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.ProductTeamGeneration;

public interface SaveProductTeamGenerationPort {

    ProductTeamGeneration save(ProductTeamGeneration generation);

    void delete(ProductTeamGeneration generation);
}
