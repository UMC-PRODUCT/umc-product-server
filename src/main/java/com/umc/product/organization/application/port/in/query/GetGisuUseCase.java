package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import com.umc.product.organization.application.port.in.query.dto.GisuNameInfo;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetGisuUseCase {

    List<GisuInfo> getList();

    Page<GisuInfo> getList(Pageable pageable);

    List<GisuNameInfo> getAllGisuNames();

    GisuInfo getById(Long gisuId);

    Long getActiveGisuId();

    GisuInfo getActiveGisu();
}
