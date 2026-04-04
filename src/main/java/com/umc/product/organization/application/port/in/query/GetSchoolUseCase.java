package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolLinkInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolNameInfo;
import com.umc.product.organization.application.port.in.query.dto.UnassignedSchoolInfo;
import java.util.List;


public interface GetSchoolUseCase {

    List<SchoolNameInfo> getAllSchoolNames();

    SchoolDetailInfo getSchoolDetail(Long schoolId);

    SchoolLinkInfo getSchoolLink(Long schoolId);

    List<UnassignedSchoolInfo> getUnassignedSchools(Long gisuId);

    List<SchoolDetailInfo> getSchoolListByGisuId(Long gisuId);
}
