package com.umc.product.authorization.adapter.in.web.dto.response;

import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;
import java.util.List;

public record BatchResourcePermissionResponse(
    List<ResourcePermissionResponse> results
) {

    public static BatchResourcePermissionResponse from(List<ResourcePermissionInfo> infos) {
        return new BatchResourcePermissionResponse(
            infos.stream()
                .map(ResourcePermissionResponse::from)
                .toList()
        );
    }
}
