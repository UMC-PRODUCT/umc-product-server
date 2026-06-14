package com.umc.product.organization.application.port.in.query;

import java.util.List;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductGenerationInfo;

public interface GetUmcProductGenerationUseCase {

    UmcProductGenerationInfo getById(Long umcProductGenerationId);

    List<UmcProductGenerationInfo> listAll();
}
