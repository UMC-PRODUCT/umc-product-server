package com.umc.product.organization.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.organization.application.port.in.query.GetUmcProductSquadUseCase;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductSquadInfo;
import com.umc.product.organization.application.port.out.query.LoadUmcProductGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductSquadPort;
import com.umc.product.organization.domain.UmcProductGeneration;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UmcProductSquadQueryService implements GetUmcProductSquadUseCase {

    private final LoadUmcProductGenerationPort loadUmcProductGenerationPort;
    private final LoadUmcProductSquadPort loadUmcProductSquadPort;

    @Override
    public List<UmcProductSquadInfo> list(Long umcProductGenerationId, Boolean active) {
        if (umcProductGenerationId == null) {
            return loadUmcProductSquadPort.listAll(active).stream()
                .map(UmcProductSquadInfo::from)
                .toList();
        }

        UmcProductGeneration generation = loadUmcProductGenerationPort.getById(umcProductGenerationId);
        return loadUmcProductSquadPort.listOverlapping(generation.getStartAt(), generation.getEndAt()).stream()
            .filter(squad -> active == null || squad.isActive() == active)
            .map(UmcProductSquadInfo::from)
            .toList();
    }
}
