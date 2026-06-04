package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberInfo;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetProductTeamMemberUseCase {

    ProductTeamMemberInfo getById(Long productTeamMemberId);

    Page<ProductTeamMemberInfo> search(ProductTeamMemberSearchCondition condition, Pageable pageable);
}
