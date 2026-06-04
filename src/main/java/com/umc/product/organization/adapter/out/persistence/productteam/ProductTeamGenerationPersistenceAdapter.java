package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.application.port.out.command.SaveProductTeamGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamGenerationPort;
import com.umc.product.organization.domain.ProductTeamGeneration;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductTeamGenerationPersistenceAdapter
    implements LoadProductTeamGenerationPort, SaveProductTeamGenerationPort {

    private final ProductTeamGenerationJpaRepository productTeamGenerationJpaRepository;

    @Override
    public ProductTeamGeneration getById(Long productTeamGenerationId) {
        return findById(productTeamGenerationId)
            .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_GENERATION_NOT_FOUND));
    }

    @Override
    public Optional<ProductTeamGeneration> findById(Long productTeamGenerationId) {
        return productTeamGenerationJpaRepository.findById(productTeamGenerationId);
    }

    @Override
    public Optional<ProductTeamGeneration> findActiveWithLock() {
        return productTeamGenerationJpaRepository.findActiveWithLock();
    }

    @Override
    public List<ProductTeamGeneration> listByIds(Collection<Long> productTeamGenerationIds) {
        if (productTeamGenerationIds == null || productTeamGenerationIds.isEmpty()) {
            return List.of();
        }
        return productTeamGenerationJpaRepository.findByIdIn(productTeamGenerationIds);
    }

    @Override
    public List<ProductTeamGeneration> findAll() {
        return productTeamGenerationJpaRepository.findAllByOrderByGenerationDesc();
    }

    @Override
    public boolean existsById(Long productTeamGenerationId) {
        return productTeamGenerationJpaRepository.existsById(productTeamGenerationId);
    }

    @Override
    public boolean existsByGeneration(Long generation) {
        return productTeamGenerationJpaRepository.existsByGeneration(generation);
    }

    @Override
    public ProductTeamGeneration save(ProductTeamGeneration generation) {
        return productTeamGenerationJpaRepository.save(generation);
    }

    @Override
    public void delete(ProductTeamGeneration generation) {
        productTeamGenerationJpaRepository.delete(generation);
    }
}
