package com.umc.product.organization.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.organization.application.port.in.query.GetUmcProductGenerationUseCase;
import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductGenerationInfo;
import com.umc.product.organization.application.port.out.query.LoadUmcProductGenerationPort;

import lombok.RequiredArgsConstructor;

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
