package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.domain.ProductTeamFunctionalUnit;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTeamFunctionalUnitJpaRepository extends JpaRepository<ProductTeamFunctionalUnit, Long> {

    List<ProductTeamFunctionalUnit> findAllByProductTeamGenerationIdOrderBySortOrderAscIdAsc(Long productTeamGenerationId);

    List<ProductTeamFunctionalUnit> findAllByProductTeamGenerationIdAndTypeOrderBySortOrderAscIdAsc(
        Long productTeamGenerationId,
        ProductTeamFunctionalUnitType type
    );

    List<ProductTeamFunctionalUnit> findByIdIn(Collection<Long> ids);
}
