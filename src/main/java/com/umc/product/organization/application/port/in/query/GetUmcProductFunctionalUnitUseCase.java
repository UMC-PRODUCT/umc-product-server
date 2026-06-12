package com.umc.product.organization.application.port.in.query;

import java.util.List;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductFunctionalUnitInfo;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;

public interface GetUmcProductFunctionalUnitUseCase {

    List<UmcProductFunctionalUnitInfo> listByGeneration(Long umcProductGenerationId, UmcProductFunctionalUnitType type);
}
