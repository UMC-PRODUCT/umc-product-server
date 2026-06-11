package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.application.port.out.command.SaveProductTeamSquadPort;
import com.umc.product.organization.application.port.out.query.LoadProductTeamSquadPort;
import com.umc.product.organization.domain.ProductTeamSquad;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductTeamSquadPersistenceAdapter implements LoadProductTeamSquadPort, SaveProductTeamSquadPort {

    private final ProductTeamSquadJpaRepository productTeamSquadJpaRepository;

    @Override
    public ProductTeamSquad getById(Long squadId) {
        return productTeamSquadJpaRepository.findById(squadId)
            .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.PRODUCT_TEAM_SQUAD_NOT_FOUND));
    }

    @Override
    public List<ProductTeamSquad> listAll(Boolean active) {
        if (active == null) {
            return productTeamSquadJpaRepository.findAllByOrderBySortOrderAscIdAsc();
        }
        return productTeamSquadJpaRepository.findAllByIsActiveOrderBySortOrderAscIdAsc(active);
    }

    @Override
    public List<ProductTeamSquad> listOverlapping(Instant startAt, Instant endAt) {
        if (startAt == null || endAt == null) {
            return List.of();
        }
        return productTeamSquadJpaRepository.findAllOverlapping(startAt, endAt);
    }

    @Override
    public List<ProductTeamSquad> listByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return productTeamSquadJpaRepository.findByIdIn(ids);
    }

    @Override
    public ProductTeamSquad save(ProductTeamSquad squad) {
        return productTeamSquadJpaRepository.save(squad);
    }

    @Override
    public void delete(ProductTeamSquad squad) {
        productTeamSquadJpaRepository.delete(squad);
    }
}
