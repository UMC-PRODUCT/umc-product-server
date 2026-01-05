package com.umc.product.organization.application.port.out;

import com.umc.product.organization.domain.CentralOrganization;

public interface SaveCentralOrganizationPort {

    CentralOrganization save(CentralOrganization organization);

    void delete(CentralOrganization organization);
}
