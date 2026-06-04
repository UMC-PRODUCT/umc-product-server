package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.ProductTeamMembership;
import java.util.Collection;

public interface SaveProductTeamMembershipPort {

    ProductTeamMembership save(ProductTeamMembership membership);

    void saveAll(Collection<ProductTeamMembership> memberships);

    void deleteAllByProductTeamMemberId(Long productTeamMemberId);
}
