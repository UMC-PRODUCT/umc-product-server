package com.umc.product.organization.application.port.in.query;

import java.util.List;

import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationQuery;

public interface GetGisuOrganizationUseCase {

    List<GisuOrganizationInfo> get(GisuOrganizationQuery query);
}
