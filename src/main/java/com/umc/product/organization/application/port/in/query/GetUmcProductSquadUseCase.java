package com.umc.product.organization.application.port.in.query;

import java.util.List;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductSquadInfo;

public interface GetUmcProductSquadUseCase {

    List<UmcProductSquadInfo> list(Long umcProductGenerationId, Boolean active);
}
