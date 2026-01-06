package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.CentralOrganization;
import java.util.Optional;

public interface CentralOrganizationManagePort {

    Optional<CentralOrganization> findById(Long id);

    CentralOrganization save(CentralOrganization organization);

    void delete(CentralOrganization organization);


}
