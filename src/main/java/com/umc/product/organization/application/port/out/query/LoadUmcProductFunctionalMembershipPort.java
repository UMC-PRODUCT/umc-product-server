package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductMemberSearchCondition;
import com.umc.product.organization.domain.UmcProductFunctionalMembership;
import com.umc.product.organization.domain.enums.UmcProductFunctionalRole;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface LoadUmcProductFunctionalMembershipPort {

    List<UmcProductFunctionalMembership> listByUmcProductMemberId(Long umcProductMemberId);

    List<UmcProductFunctionalMembership> listByUmcProductMemberIds(Collection<Long> umcProductMemberIds,
                                                                     UmcProductMemberSearchCondition condition);

    List<Long> listGenerationIdsByUmcProductMemberId(Long umcProductMemberId);

    boolean existsByMemberIdAndGenerationIdAndRoles(Long memberId, Long umcProductGenerationId,
                                                    Set<UmcProductFunctionalRole> roles);

    boolean existsByMemberIdAndActiveGenerationAndRoles(Long memberId, Set<UmcProductFunctionalRole> roles);
}
