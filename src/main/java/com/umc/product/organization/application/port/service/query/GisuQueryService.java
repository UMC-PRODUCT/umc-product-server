package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import com.umc.product.organization.application.port.in.query.dto.GisuNameInfo;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<GisuInfo> getList(Pageable pageable) {
        return loadGisuPort.findAll(pageable)
                .map(GisuInfo::from);
    }

    @Override
    public List<GisuNameInfo> getAllGisuNames() {
        return loadGisuPort.findAll().stream()
                .map(GisuNameInfo::from)
                .toList();
    }

    @Override
    public GisuInfo getById(Long gisuId) {
        return GisuInfo.from(loadGisuPort.findById(gisuId));
    }

    @Override
    public List<GisuInfo> getByIds(Set<Long> gisuIds) {
        return loadGisuPort.findByIds(gisuIds).stream()
                .map(GisuInfo::from)
                .toList();
    }

    @Override
    public Long getActiveGisuId() {
        return loadGisuPort.findActiveGisu().getId();
    }

    @Override
    public GisuInfo getActiveGisu() {
        return GisuInfo.from(loadGisuPort.findActiveGisu());
    }
}
