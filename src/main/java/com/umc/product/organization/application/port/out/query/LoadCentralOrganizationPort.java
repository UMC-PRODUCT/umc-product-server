package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.CentralOrganization;
import java.util.Optional;

public interface LoadCentralOrganizationPort {

    Optional<CentralOrganization> findById(Long id);

}
