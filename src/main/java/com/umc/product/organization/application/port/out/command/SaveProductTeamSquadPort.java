package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.ProductTeamSquad;

public interface SaveProductTeamSquadPort {

    ProductTeamSquad save(ProductTeamSquad squad);

    void delete(ProductTeamSquad squad);
}
