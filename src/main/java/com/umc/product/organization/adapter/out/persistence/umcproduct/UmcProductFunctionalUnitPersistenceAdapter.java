package com.umc.product.organization.adapter.out.persistence.umcproduct;

import com.umc.product.organization.application.port.out.command.SaveUmcProductFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalUnitPort;
import com.umc.product.organization.domain.UmcProductFunctionalUnit;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UmcProductFunctionalUnitPersistenceAdapter
    implements LoadUmcProductFunctionalUnitPort, SaveUmcProductFunctionalUnitPort {

    private final UmcProductFunctionalUnitJpaRepository umcProductFunctionalUnitJpaRepository;

    @Override
    public UmcProductFunctionalUnit getById(Long functionalUnitId) {
        return umcProductFunctionalUnitJpaRepository.findById(functionalUnitId)
            .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_FUNCTIONAL_UNIT_NOT_FOUND));
    }

    @Override
    public List<UmcProductFunctionalUnit> listByGenerationId(Long umcProductGenerationId) {
        return umcProductFunctionalUnitJpaRepository
            .findAllByUmcProductGenerationIdOrderBySortOrderAscIdAsc(umcProductGenerationId);
    }

    @Override
    public List<UmcProductFunctionalUnit> listByGenerationIdAndType(
        Long umcProductGenerationId,
        UmcProductFunctionalUnitType type
    ) {
        if (type == null) {
            return listByGenerationId(umcProductGenerationId);
        }
        return umcProductFunctionalUnitJpaRepository
            .findAllByUmcProductGenerationIdAndTypeOrderBySortOrderAscIdAsc(umcProductGenerationId, type);
    }

    @Override
    public List<UmcProductFunctionalUnit> listByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return umcProductFunctionalUnitJpaRepository.findByIdIn(ids);
    }

    @Override
    public UmcProductFunctionalUnit save(UmcProductFunctionalUnit functionalUnit) {
        return umcProductFunctionalUnitJpaRepository.save(functionalUnit);
    }

    @Override
    public void delete(UmcProductFunctionalUnit functionalUnit) {
        umcProductFunctionalUnitJpaRepository.delete(functionalUnit);
    }
}
