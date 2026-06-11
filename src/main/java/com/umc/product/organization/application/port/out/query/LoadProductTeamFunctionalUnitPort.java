package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.ProductTeamFunctionalUnit;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;
import java.util.Collection;
import java.util.List;

public interface LoadProductTeamFunctionalUnitPort {

    ProductTeamFunctionalUnit getById(Long functionalUnitId);

    List<ProductTeamFunctionalUnit> listByGenerationId(Long productTeamGenerationId);

    List<ProductTeamFunctionalUnit> listByGenerationIdAndType(Long productTeamGenerationId,
                                                              ProductTeamFunctionalUnitType type);

    List<ProductTeamFunctionalUnit> listByIds(Collection<Long> ids);
}
