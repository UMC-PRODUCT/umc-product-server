package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.application.port.out.command.SaveProductTeamFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamFunctionalUnitPort;
import com.umc.product.organization.domain.ProductTeamFunctionalUnit;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductTeamFunctionalUnitPersistenceAdapter
    implements LoadProductTeamFunctionalUnitPort, SaveProductTeamFunctionalUnitPort {

    private final ProductTeamFunctionalUnitJpaRepository productTeamFunctionalUnitJpaRepository;

    @Override
    public ProductTeamFunctionalUnit getById(Long functionalUnitId) {
        return productTeamFunctionalUnitJpaRepository.findById(functionalUnitId)
            .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_FUNCTIONAL_UNIT_NOT_FOUND));
    }

    @Override
    public List<ProductTeamFunctionalUnit> listByGenerationId(Long productTeamGenerationId) {
        return productTeamFunctionalUnitJpaRepository
            .findAllByProductTeamGenerationIdOrderBySortOrderAscIdAsc(productTeamGenerationId);
    }

    @Override
    public List<ProductTeamFunctionalUnit> listByGenerationIdAndType(
        Long productTeamGenerationId,
        ProductTeamFunctionalUnitType type
    ) {
        if (type == null) {
            return listByGenerationId(productTeamGenerationId);
        }
        return productTeamFunctionalUnitJpaRepository
            .findAllByProductTeamGenerationIdAndTypeOrderBySortOrderAscIdAsc(productTeamGenerationId, type);
    }

    @Override
    public List<ProductTeamFunctionalUnit> listByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productTeamFunctionalUnitJpaRepository.findByIdIn(ids);
    }

    @Override
    public ProductTeamFunctionalUnit save(ProductTeamFunctionalUnit functionalUnit) {
        return productTeamFunctionalUnitJpaRepository.save(functionalUnit);
    }

    @Override
    public void delete(ProductTeamFunctionalUnit functionalUnit) {
        productTeamFunctionalUnitJpaRepository.delete(functionalUnit);
    }
}
