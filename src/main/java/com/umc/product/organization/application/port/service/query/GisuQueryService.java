package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GisuQueryService implements GetGisuUseCase {

    private final LoadGisuPort loadGisuPort;

    @Override
    public List<GisuInfo> getList() {
        return loadGisuPort.findAll().stream()
                .map(GisuInfo::from)
                .toList();
    }

    @Override
    public GisuInfo getById(Long gisuId) {
        return GisuInfo.from(loadGisuPort.findById(gisuId));
    }
}
