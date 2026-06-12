package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductGenerationInfo;
import java.util.List;

public interface GetUmcProductGenerationUseCase {

    UmcProductGenerationInfo getById(Long umcProductGenerationId);

    List<UmcProductGenerationInfo> listAll();
}
