package com.umc.product.organization.application.port.in.query;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.umc.product.organization.application.port.in.query.dto.school.SchoolChapterNameInfo;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.school.SchoolNameInfo;


public interface GetSchoolUseCase {

    List<SchoolNameInfo> getAllSchoolNames();

    SchoolDetailInfo getSchoolDetail(Long schoolId);

    List<SchoolDetailInfo> listDetailsByIds(Set<Long> schoolIds);

    List<SchoolDetailInfo> getSchoolListByGisuId(Long gisuId);

    List<SchoolChapterNameInfo> getSchoolChapterNamesByGisuId(Long gisuId);

    Map<Long, List<SchoolDetailInfo>> getSchoolListByGisuIds(Set<Long> gisuIds);
}
