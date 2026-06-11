package com.umc.product.organization.application.service;

import com.umc.product.organization.application.port.in.query.GetProductTeamSquadUseCase;
import com.umc.product.organization.application.port.in.query.dto.productteam.ProductTeamSquadInfo;
import com.umc.product.organization.application.port.out.query.LoadProductTeamGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamSquadPort;
import com.umc.product.organization.domain.ProductTeamGeneration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductTeamSquadQueryService implements GetProductTeamSquadUseCase {

    private final LoadProductTeamGenerationPort loadProductTeamGenerationPort;
    private final LoadProductTeamSquadPort loadProductTeamSquadPort;

    @Override
    public List<ProductTeamSquadInfo> list(Long productTeamGenerationId, Boolean active) {
        if (productTeamGenerationId == null) {
            return loadProductTeamSquadPort.listAll(active).stream()
                .map(ProductTeamSquadInfo::from)
                .toList();
        }

        ProductTeamGeneration generation = loadProductTeamGenerationPort.getById(productTeamGenerationId);
        return loadProductTeamSquadPort.listOverlapping(generation.getStartAt(), generation.getEndAt()).stream()
            .filter(squad -> active == null || squad.isActive() == active)
            .map(ProductTeamSquadInfo::from)
            .toList();
    }
}
