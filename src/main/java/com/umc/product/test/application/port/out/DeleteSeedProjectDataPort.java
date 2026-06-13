package com.umc.product.test.application.port.out;

import com.umc.product.test.application.port.out.dto.ProjectDataDeletionCounts;

public interface DeleteSeedProjectDataPort {

    ProjectDataDeletionCounts deleteByGisuId(Long gisuId);
}
