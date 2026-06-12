package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.UmcProductFunctionalMembership;
import java.util.Collection;

public interface SaveUmcProductFunctionalMembershipPort {

    UmcProductFunctionalMembership save(UmcProductFunctionalMembership functionalMembership);

    void saveAll(Collection<UmcProductFunctionalMembership> functionalMemberships);

    void deleteAllByUmcProductMemberId(Long umcProductMemberId);
}
