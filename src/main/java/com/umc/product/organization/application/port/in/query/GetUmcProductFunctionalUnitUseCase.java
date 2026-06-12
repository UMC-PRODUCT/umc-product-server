package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductFunctionalUnitInfo;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;
import java.util.List;

public interface GetUmcProductFunctionalUnitUseCase {

    List<UmcProductFunctionalUnitInfo> listByGeneration(Long umcProductGenerationId, UmcProductFunctionalUnitType type);
}
