package com.umc.product.query.organization.application.port.in;

import com.umc.product.command.organization.application.port.in.dto.response.DeletableSchoolSummary;
import com.umc.product.command.organization.application.port.in.dto.response.SchoolDeleteSearchCondition;
import com.umc.product.command.organization.application.port.in.dto.response.SchoolInfo;
import com.umc.product.command.organization.application.port.in.dto.response.SchoolSummary;
import com.umc.product.common.dto.request.PageRequest;
import com.umc.product.common.dto.request.PageResult;

import java.util.List;


public interface SchoolQueryUseCase {

    List<SchoolSummary> getAll(Long gisuId);

    SchoolInfo getById(Long schoolId);

    PageResult<DeletableSchoolSummary> getList(SchoolDeleteSearchCondition condition, PageRequest pageRequest);

}
