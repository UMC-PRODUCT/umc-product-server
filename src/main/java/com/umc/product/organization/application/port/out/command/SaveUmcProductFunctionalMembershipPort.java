package com.umc.product.organization.application.port.out.command;

import java.util.Collection;

import com.umc.product.organization.domain.UmcProductFunctionalMembership;

public interface SaveUmcProductFunctionalMembershipPort {

    UmcProductFunctionalMembership save(UmcProductFunctionalMembership functionalMembership);

    void saveAll(Collection<UmcProductFunctionalMembership> functionalMemberships);

    void deleteAllByUmcProductMemberId(Long umcProductMemberId);
}
