package com.umc.product.organization.adapter.out.persistence.umcproduct;

import com.umc.product.organization.domain.UmcProductFunctionalUnit;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UmcProductFunctionalUnitJpaRepository extends JpaRepository<UmcProductFunctionalUnit, Long> {

    List<UmcProductFunctionalUnit> findAllByUmcProductGenerationIdOrderBySortOrderAscIdAsc(Long umcProductGenerationId);

    List<UmcProductFunctionalUnit> findAllByUmcProductGenerationIdAndTypeOrderBySortOrderAscIdAsc(
        Long umcProductGenerationId,
        UmcProductFunctionalUnitType type
    );

    List<UmcProductFunctionalUnit> findByIdIn(Collection<Long> ids);
}
