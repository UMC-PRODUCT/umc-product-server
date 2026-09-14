package com.umc.product.authorization.adapter.in.web.dto.response;

import java.util.List;

import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionInfo;

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
