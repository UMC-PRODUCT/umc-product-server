package com.umc.product.organization.application.service;

import com.umc.product.organization.application.port.in.query.GetUmcProductGenerationUseCase;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductGenerationInfo;
import com.umc.product.organization.application.port.out.query.LoadUmcProductGenerationPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UmcProductGenerationQueryService implements GetUmcProductGenerationUseCase {

    private final LoadUmcProductGenerationPort loadUmcProductGenerationPort;

    @Override
    public UmcProductGenerationInfo getById(Long umcProductGenerationId) {
        return UmcProductGenerationInfo.from(loadUmcProductGenerationPort.getById(umcProductGenerationId));
    }

    @Override
    public List<UmcProductGenerationInfo> listAll() {
        return loadUmcProductGenerationPort.findAll().stream()
            .map(UmcProductGenerationInfo::from)
            .toList();
    }
}
