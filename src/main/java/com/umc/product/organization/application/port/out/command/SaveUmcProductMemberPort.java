package com.umc.product.organization.application.port.out.command;

import com.umc.product.organization.domain.UmcProductMember;

public interface SaveUmcProductMemberPort {

    UmcProductMember save(UmcProductMember member);

    void delete(UmcProductMember member);
}
