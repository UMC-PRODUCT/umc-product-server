package com.umc.product.organization.application.port.in.query;

import com.umc.product.organization.application.port.in.query.dto.school.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolLinkInfo;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolNameInfo;
import com.umc.product.organization.application.port.in.query.dto.school.UnassignedSchoolInfo;
import java.util.List;


public interface GetSchoolUseCase {

    List<SchoolNameInfo> getAllSchoolNames();

    SchoolDetailInfo getSchoolDetail(Long schoolId);

    SchoolLinkInfo getSchoolLink(Long schoolId);

    List<UnassignedSchoolInfo> getUnassignedSchools(Long gisuId);

    List<SchoolDetailInfo> getSchoolListByGisuId(Long gisuId);
}
