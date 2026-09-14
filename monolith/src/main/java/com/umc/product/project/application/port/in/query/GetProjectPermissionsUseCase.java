package com.umc.product.project.application.port.in.query;

import java.util.List;

import com.umc.product.project.application.port.in.query.dto.ProjectPermissionInfo;

public interface GetProjectPermissionsUseCase {

    List<ProjectPermissionInfo> listByProjectIds(Long requesterMemberId, List<Long> projectIds);
}
