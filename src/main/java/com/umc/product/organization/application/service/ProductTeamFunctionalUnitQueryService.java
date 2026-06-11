package com.umc.product.organization.application.service;

import com.umc.product.organization.application.port.in.query.GetProductTeamFunctionalUnitUseCase;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamFunctionalUnitInfo;
import com.umc.product.organization.application.port.out.query.LoadProductTeamFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamGenerationPort;
import com.umc.product.organization.domain.enums.ProductTeamFunctionalUnitType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductTeamFunctionalUnitQueryService implements GetProductTeamFunctionalUnitUseCase {

    private final LoadProductTeamGenerationPort loadProductTeamGenerationPort;
    private final LoadProductTeamFunctionalUnitPort loadProductTeamFunctionalUnitPort;

    @Override
    public List<ProductTeamFunctionalUnitInfo> listByGeneration(
        Long productTeamGenerationId,
        ProductTeamFunctionalUnitType type
    ) {
        loadProductTeamGenerationPort.getById(productTeamGenerationId);
        return loadProductTeamFunctionalUnitPort.listByGenerationIdAndType(productTeamGenerationId, type).stream()
            .map(ProductTeamFunctionalUnitInfo::from)
            .toList();
    }
}
