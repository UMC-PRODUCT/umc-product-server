package com.umc.product.command.organization.application.port.out;

import com.umc.product.command.organization.domain.CentralOrganization;
import java.util.Optional;

public interface CentralOrganizationCommandPort {

    Optional<CentralOrganization> findById(Long id);

    CentralOrganization save(CentralOrganization organization);

    void delete(CentralOrganization organization);


}
