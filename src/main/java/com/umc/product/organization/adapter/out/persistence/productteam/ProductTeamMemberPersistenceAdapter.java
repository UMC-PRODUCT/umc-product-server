package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamMemberSearchCondition;
import com.umc.product.organization.application.port.out.command.SaveProductTeamMemberPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamMemberPort;
import com.umc.product.organization.domain.ProductTeamMember;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductTeamMemberPersistenceAdapter implements LoadProductTeamMemberPort, SaveProductTeamMemberPort {

    private final ProductTeamMemberJpaRepository productTeamMemberJpaRepository;
    private final ProductTeamMemberQueryRepository productTeamMemberQueryRepository;

    @Override
    public ProductTeamMember getById(Long productTeamMemberId) {
        return findById(productTeamMemberId)
            .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_MEMBER_NOT_FOUND));
    }

    @Override
    public Optional<ProductTeamMember> findById(Long productTeamMemberId) {
        return productTeamMemberJpaRepository.findById(productTeamMemberId);
    }

    @Override
    public ProductTeamMember getByMemberId(Long memberId) {
        return findByMemberId(memberId)
            .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_MEMBER_NOT_FOUND));
    }

    @Override
    public Optional<ProductTeamMember> findByMemberId(Long memberId) {
        return productTeamMemberJpaRepository.findByMemberId(memberId);
    }

    @Override
    public List<ProductTeamMember> listByIds(Collection<Long> productTeamMemberIds) {
        if (productTeamMemberIds == null || productTeamMemberIds.isEmpty()) {
            return List.of();
        }
        return productTeamMemberJpaRepository.findByIdIn(productTeamMemberIds);
    }

    @Override
    public Page<Long> searchIds(ProductTeamMemberSearchCondition condition, Pageable pageable) {
        return productTeamMemberQueryRepository.searchMemberIds(condition, pageable);
    }

    @Override
    public boolean existsByMemberId(Long memberId) {
        return productTeamMemberJpaRepository.existsByMemberId(memberId);
    }

    @Override
    public ProductTeamMember save(ProductTeamMember member) {
        return productTeamMemberJpaRepository.save(member);
    }

    @Override
    public void delete(ProductTeamMember member) {
        productTeamMemberJpaRepository.delete(member);
    }
}
