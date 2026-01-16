package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadSchoolPort {

    Page<SchoolListItemInfo> getSchools(SchoolSearchCondition condition, Pageable pageable);
}
