package com.umc.product.organization.application.service;

import com.umc.product.organization.application.port.in.query.GetProductTeamGenerationUseCase;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamGenerationInfo;
import com.umc.product.organization.application.port.out.query.LoadProductTeamGenerationPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductTeamGenerationQueryService implements GetProductTeamGenerationUseCase {

    private final LoadProductTeamGenerationPort loadProductTeamGenerationPort;

    @Override
    public ProductTeamGenerationInfo getById(Long productTeamGenerationId) {
        return ProductTeamGenerationInfo.from(loadProductTeamGenerationPort.getById(productTeamGenerationId));
    }

    @Override
    public List<ProductTeamGenerationInfo> listAll() {
        return loadProductTeamGenerationPort.findAll().stream()
            .map(ProductTeamGenerationInfo::from)
            .toList();
    }
}
