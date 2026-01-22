package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import java.util.List;

public interface GetGisuUseCase {

    List<GisuInfo> getList();

    GisuInfo getById(Long gisuId);

    Long getActiveGisuId();
}
