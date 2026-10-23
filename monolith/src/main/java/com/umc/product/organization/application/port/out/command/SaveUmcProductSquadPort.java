package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.UmcProductSquad;

public interface SaveUmcProductSquadPort {

    UmcProductSquad save(UmcProductSquad squad);

    void delete(UmcProductSquad squad);
}
