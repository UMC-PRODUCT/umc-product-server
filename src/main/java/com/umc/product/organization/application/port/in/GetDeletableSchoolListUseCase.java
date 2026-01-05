package com.umc.product.organization.application.port.in;

import com.umc.product.organization.application.port.in.dto.DeletableSchoolSummary;
import com.umc.product.organization.application.port.in.dto.SchoolDeleteSearchCondition;
import com.umc.product.organization.application.port.in.dto.paging.PageRequest;
import com.umc.product.organization.application.port.in.dto.paging.PageResult;

public interface GetDeletableSchoolListUseCase {
    PageResult<DeletableSchoolSummary> getList(SchoolDeleteSearchCondition condition, PageRequest pageRequest);

}
