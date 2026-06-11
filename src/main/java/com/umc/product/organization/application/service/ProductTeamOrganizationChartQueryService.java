package com.umc.product.organization.application.service;

import com.umc.product.organization.application.port.in.query.GetProductTeamOrganizationChartUseCase;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamFunctionalUnitInfo;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamGenerationInfo;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamOrganizationChartInfo;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamSquadInfo;
import com.umc.product.organization.application.port.out.query.LoadProductTeamFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamSquadPort;
import com.umc.product.organization.domain.ProductTeamGeneration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductTeamOrganizationChartQueryService implements GetProductTeamOrganizationChartUseCase {

    private final LoadProductTeamGenerationPort loadProductTeamGenerationPort;
    private final LoadProductTeamFunctionalUnitPort loadProductTeamFunctionalUnitPort;
    private final LoadProductTeamSquadPort loadProductTeamSquadPort;

    @Override
    public ProductTeamOrganizationChartInfo getByGenerationId(Long productTeamGenerationId) {
        ProductTeamGeneration generation = loadProductTeamGenerationPort.getById(productTeamGenerationId);
        return new ProductTeamOrganizationChartInfo(
            ProductTeamGenerationInfo.from(generation),
            loadProductTeamFunctionalUnitPort.listByGenerationId(productTeamGenerationId).stream()
                .map(ProductTeamFunctionalUnitInfo::from)
                .toList(),
            loadProductTeamSquadPort.listOverlapping(generation.getStartAt(), generation.getEndAt()).stream()
                .map(ProductTeamSquadInfo::from)
                .toList()
        );
    }
}
