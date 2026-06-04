package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.domain.ProductTeamMember;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTeamMemberJpaRepository extends JpaRepository<ProductTeamMember, Long> {

    Optional<ProductTeamMember> findByMemberId(Long memberId);

    boolean existsByMemberId(Long memberId);

    List<ProductTeamMember> findByIdIn(Collection<Long> ids);
}
