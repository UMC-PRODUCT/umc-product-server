package com.umc.product.project.application.port.in.query;

import com.umc.product.project.application.port.in.query.dto.ProjectQuotaManagementInfo;

public interface GetProjectQuotaManagementUseCase {
    ProjectQuotaManagementInfo get(Long memberId, Long gisuId, Long chapterId, String keyword, Long schoolId);
}
