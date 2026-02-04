package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.GisuInfo;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetGisuUseCase {

    List<GisuInfo> getList();

    Page<GisuInfo> getList(Pageable pageable);

    GisuInfo getById(Long gisuId);

    Long getActiveGisuId();
}
