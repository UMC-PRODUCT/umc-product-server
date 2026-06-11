package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberSearchCondition;
import com.umc.product.organization.domain.ProductTeamFunctionalMembership;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalRole;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface LoadProductTeamFunctionalMembershipPort {

    List<ProductTeamFunctionalMembership> listByProductTeamMemberId(Long productTeamMemberId);

    List<ProductTeamFunctionalMembership> listByProductTeamMemberIds(Collection<Long> productTeamMemberIds,
                                                                     ProductTeamMemberSearchCondition condition);

    List<Long> listGenerationIdsByProductTeamMemberId(Long productTeamMemberId);

    boolean existsByMemberIdAndGenerationIdAndRoles(Long memberId, Long productTeamGenerationId,
                                                    Set<ProductTeamFunctionalRole> roles);

    boolean existsByMemberIdAndActiveGenerationAndRoles(Long memberId, Set<ProductTeamFunctionalRole> roles);
}
