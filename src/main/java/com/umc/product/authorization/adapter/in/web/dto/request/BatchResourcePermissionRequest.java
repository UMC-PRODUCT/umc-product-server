package com.umc.product.authorization.adapter.in.web.dto.request;

import com.umc.product.authorization.application.port.in.query.dto.ResourcePermissionQuery;
import com.umc.product.authorization.domain.PermissionType;
import com.umc.product.authorization.domain.ResourceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BatchResourcePermissionRequest(
    @NotEmpty(message = "queries는 최소 1개 이상이어야 합니다.")
    List<@NotNull(message = "query는 null일 수 없습니다.") @Valid ResourcePermissionQueryRequest> queries
) {

    public List<ResourcePermissionQuery> toQueries() {
        return queries.stream()
            .map(ResourcePermissionQueryRequest::toQuery)
            .toList();
    }

    public record ResourcePermissionQueryRequest(
        @NotNull(message = "resourceType은 필수입니다.")
        ResourceType resourceType,
        List<@NotNull(message = "resourceIds는 null 값을 포함할 수 없습니다.") Long> resourceIds,
        List<@NotNull(message = "permissionTypes는 null 값을 포함할 수 없습니다.") PermissionType> permissionTypes
    ) {

        public ResourcePermissionQuery toQuery() {
            return ResourcePermissionQuery.of(resourceType, resourceIds, permissionTypes);
        }
    }
}
