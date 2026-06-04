package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.ProductTeamMember;

public interface SaveProductTeamMemberPort {

    ProductTeamMember save(ProductTeamMember member);

    void delete(ProductTeamMember member);
}
