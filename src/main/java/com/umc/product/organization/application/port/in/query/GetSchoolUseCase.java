package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.SchoolInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolLinkInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface GetSchoolUseCase {

    Page<SchoolListItemInfo> getSchools(SchoolSearchCondition condition, Pageable pageable);

    SchoolInfo getSchoolDetail(Long schoolId);

    SchoolLinkInfo getSchoolLink(Long schoolId);
}
