package com.umc.product.organization.application.port.in.query;

import com.umc.product.common.dto.request.PageRequest;
import com.umc.product.common.dto.request.PageResult;
import com.umc.product.organization.application.port.in.query.dto.DeletableSchoolSummary;
import com.umc.product.organization.application.port.in.query.dto.SchoolDeleteSearchCondition;
import com.umc.product.organization.application.port.in.query.dto.SchoolInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSummary;
import java.util.List;


public interface GetSchoolUseCase {

    List<SchoolSummary> getAll(Long gisuId);

    SchoolInfo getById(Long schoolId);

    PageResult<DeletableSchoolSummary> getList(SchoolDeleteSearchCondition condition, PageRequest pageRequest);

}
