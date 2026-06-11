package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.ProductTeamFunctionalMembership;
import java.util.Collection;

public interface SaveProductTeamFunctionalMembershipPort {

    ProductTeamFunctionalMembership save(ProductTeamFunctionalMembership functionalMembership);

    void saveAll(Collection<ProductTeamFunctionalMembership> functionalMemberships);

    void deleteAllByProductTeamMemberId(Long productTeamMemberId);
}
