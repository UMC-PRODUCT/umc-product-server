package com.umc.product.project.adapter.in.web.dto.response;

import java.util.List;

public record SaveProjectQuotaManagementResponse(List<Long> updatedProjectIds) {
    public static SaveProjectQuotaManagementResponse from(List<Long> projectIds) {
        return new SaveProjectQuotaManagementResponse(List.copyOf(projectIds));
    }
}
