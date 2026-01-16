package com.umc.product.organization.application.port.service.query;

import com.umc.product.organization.application.port.in.query.GetSchoolUseCase;
import com.umc.product.organization.application.port.in.query.dto.SchoolInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchoolQueryService implements GetSchoolUseCase {

    private final LoadSchoolPort loadSchoolPort;

    @Override
    public Page<SchoolListItemInfo> getSchools(SchoolSearchCondition condition, Pageable pageable) {
        return loadSchoolPort.getSchools(condition, pageable);
    }

    @Override
    public SchoolInfo getSchoolDetail(Long schoolId) {
        return null;
    }
}
