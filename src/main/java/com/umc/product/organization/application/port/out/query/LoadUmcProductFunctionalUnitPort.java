package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.UmcProductFunctionalUnit;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import java.util.Collection;
import java.util.List;

public interface LoadUmcProductFunctionalUnitPort {

    UmcProductFunctionalUnit getById(Long functionalUnitId);

    List<UmcProductFunctionalUnit> listByGenerationId(Long umcProductGenerationId);

    List<UmcProductFunctionalUnit> listByGenerationIdAndType(Long umcProductGenerationId,
                                                              UmcProductFunctionalUnitType type);

    List<UmcProductFunctionalUnit> listByIds(Collection<Long> ids);
}
