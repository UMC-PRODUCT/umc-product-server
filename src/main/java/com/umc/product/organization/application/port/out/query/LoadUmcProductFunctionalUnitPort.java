package com.umc.product.organization.application.port.out.query;

import java.util.Collection;
import java.util.List;

import com.umc.product.organization.domain.UmcProductFunctionalUnit;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;

public interface LoadUmcProductFunctionalUnitPort {

    UmcProductFunctionalUnit getById(Long functionalUnitId);

    List<UmcProductFunctionalUnit> listByGenerationId(Long umcProductGenerationId);

    List<UmcProductFunctionalUnit> listByGenerationIdAndType(Long umcProductGenerationId,
                                                              UmcProductFunctionalUnitType type);

    List<UmcProductFunctionalUnit> listByIds(Collection<Long> ids);
}
