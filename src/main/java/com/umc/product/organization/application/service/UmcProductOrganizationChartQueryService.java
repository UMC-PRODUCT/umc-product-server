package com.umc.product.organization.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.organization.application.port.in.query.GetUmcProductOrganizationChartUseCase;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductFunctionalUnitInfo;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductGenerationInfo;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductOrganizationChartInfo;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductSquadInfo;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductSquadPort;
import com.umc.product.organization.domain.UmcProductGeneration;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UmcProductOrganizationChartQueryService implements GetUmcProductOrganizationChartUseCase {

    private final LoadUmcProductGenerationPort loadUmcProductGenerationPort;
    private final LoadUmcProductFunctionalUnitPort loadUmcProductFunctionalUnitPort;
    private final LoadUmcProductSquadPort loadUmcProductSquadPort;

    @Override
    public UmcProductOrganizationChartInfo getByGenerationId(Long umcProductGenerationId) {
        UmcProductGeneration generation = loadUmcProductGenerationPort.getById(umcProductGenerationId);
        return new UmcProductOrganizationChartInfo(
            UmcProductGenerationInfo.from(generation),
            loadUmcProductFunctionalUnitPort.listByGenerationId(umcProductGenerationId).stream()
                .map(UmcProductFunctionalUnitInfo::from)
                .toList(),
            loadUmcProductSquadPort.listOverlapping(generation.getStartAt(), generation.getEndAt()).stream()
                .map(UmcProductSquadInfo::from)
                .toList()
        );
    }
}
