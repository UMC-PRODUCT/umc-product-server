package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolLinkInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolNameInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import com.umc.product.organization.application.port.in.query.dto.UnassignedSchoolInfo;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface GetSchoolUseCase {

    Page<SchoolListItemInfo> getSchools(SchoolSearchCondition condition, Pageable pageable);

    List<SchoolNameInfo> getAllSchoolNames();

    SchoolDetailInfo getSchoolDetail(Long schoolId);

    SchoolLinkInfo getSchoolLink(Long schoolId);

    List<UnassignedSchoolInfo> getUnassignedSchools(Long gisuId);
}
