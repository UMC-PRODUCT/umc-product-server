package com.umc.product.organization.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.organization.application.port.in.query.GetUmcProductFunctionalUnitUseCase;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductFunctionalUnitInfo;
import com.umc.product.organization.application.port.out.query.LoadUmcProductFunctionalUnitPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductGenerationPort;
import com.umc.product.organization.domain.enums.UmcProductFunctionalUnitType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UmcProductFunctionalUnitQueryService implements GetUmcProductFunctionalUnitUseCase {

    private final LoadUmcProductGenerationPort loadUmcProductGenerationPort;
    private final LoadUmcProductFunctionalUnitPort loadUmcProductFunctionalUnitPort;

    @Override
    public List<UmcProductFunctionalUnitInfo> listByGeneration(
        Long umcProductGenerationId,
        UmcProductFunctionalUnitType type
    ) {
        loadUmcProductGenerationPort.getById(umcProductGenerationId);
        return loadUmcProductFunctionalUnitPort.listByGenerationIdAndType(umcProductGenerationId, type).stream()
            .map(UmcProductFunctionalUnitInfo::from)
            .toList();
    }
}
