package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GisuQueryService implements GetGisuUseCase {
    @Override
    public List<GisuInfo> getList() {
        return List.of();
    }

    @Override
    public GisuInfo getById(Long gisuId) {
        return null;
    }
}
