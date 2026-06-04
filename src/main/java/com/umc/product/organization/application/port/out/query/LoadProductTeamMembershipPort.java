package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.ProductTeamMembership;
import com.umc.product.organization.domain.enums.ProductTeamRole;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberSearchCondition;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface LoadProductTeamMembershipPort {

    List<ProductTeamMembership> listByProductTeamMemberId(Long productTeamMemberId);

    List<ProductTeamMembership> listByProductTeamMemberIds(Collection<Long> productTeamMemberIds,
                                                           ProductTeamMemberSearchCondition condition);

    List<Long> listGenerationIdsByProductTeamMemberId(Long productTeamMemberId);

    boolean existsByMemberIdAndGenerationIdAndRoles(Long memberId, Long productTeamGenerationId,
                                                    Set<ProductTeamRole> roles);

    boolean existsByMemberIdAndGenerationIdInAndRoles(Long memberId, Collection<Long> productTeamGenerationIds,
                                                      Set<ProductTeamRole> roles);
}
