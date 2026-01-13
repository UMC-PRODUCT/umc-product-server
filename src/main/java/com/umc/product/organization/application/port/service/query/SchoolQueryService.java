package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SchoolQueryService implements GetSchoolUseCase {


    @Override
    public Page<SchoolListItemInfo> getList(SchoolSearchCondition condition, Pageable pageable) {
        return null;
    }

    @Override
    public SchoolInfo getSchoolDetail(Long schoolId) {
        return null;
    }
}
