package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberSearchCondition;
import com.umc.product.organization.domain.ProductTeamMember;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadProductTeamMemberPort {

    ProductTeamMember getById(Long productTeamMemberId);

    Optional<ProductTeamMember> findById(Long productTeamMemberId);

    ProductTeamMember getByMemberId(Long memberId);

    Optional<ProductTeamMember> findByMemberId(Long memberId);

    List<ProductTeamMember> listByIds(Collection<Long> productTeamMemberIds);

    Page<Long> searchIds(ProductTeamMemberSearchCondition condition, Pageable pageable);

    boolean existsByMemberId(Long memberId);
}
