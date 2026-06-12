package com.umc.product.organization.adapter.out.persistence.umcproduct;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.umc.product.organization.application.port.out.command.SaveUmcProductSquadPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductSquadPort;
import com.umc.product.organization.domain.UmcProductSquad;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UmcProductSquadPersistenceAdapter implements LoadUmcProductSquadPort, SaveUmcProductSquadPort {

    private final UmcProductSquadJpaRepository umcProductSquadJpaRepository;

    @Override
    public UmcProductSquad getById(Long squadId) {
        return umcProductSquadJpaRepository.findById(squadId)
            .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_SQUAD_NOT_FOUND));
    }

    @Override
    public List<UmcProductSquad> listAll(Boolean active) {
        if (active == null) {
            return umcProductSquadJpaRepository.findAllByOrderBySortOrderAscIdAsc();
        }
        return umcProductSquadJpaRepository.findAllByIsActiveOrderBySortOrderAscIdAsc(active);
    }

    @Override
    public List<UmcProductSquad> listOverlapping(Instant startAt, Instant endAt) {
        if (startAt == null || endAt == null) {
            return List.of();
        }
        return umcProductSquadJpaRepository.findAllOverlapping(startAt, endAt);
    }

    @Override
    public List<UmcProductSquad> listByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return umcProductSquadJpaRepository.findByIdIn(ids);
    }

    @Override
    public UmcProductSquad save(UmcProductSquad squad) {
        return umcProductSquadJpaRepository.save(squad);
    }

    @Override
    public void delete(UmcProductSquad squad) {
        umcProductSquadJpaRepository.delete(squad);
    }
}
