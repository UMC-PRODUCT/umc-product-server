package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.umcproduct.UmcProductSquadInfo;
import java.util.List;

public interface GetUmcProductSquadUseCase {

    List<UmcProductSquadInfo> list(Long umcProductGenerationId, Boolean active);
}
