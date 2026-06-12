package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationInfo;
import com.umc.product.organization.application.port.in.query.dto.gisu.GisuOrganizationQuery;
import java.util.List;

public interface GetGisuOrganizationUseCase {

    List<GisuOrganizationInfo> get(GisuOrganizationQuery query);
}
