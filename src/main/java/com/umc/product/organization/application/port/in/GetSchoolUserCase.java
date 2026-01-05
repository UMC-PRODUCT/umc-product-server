package com.umc.product.organization.application.port.in;


import com.umc.product.organization.application.port.in.dto.SchoolInfo;
import com.umc.product.organization.application.port.in.dto.SchoolSummary;
import java.util.List;

public interface GetSchoolUserCase {
    List<SchoolSummary> getAll(Long gisuId);
    SchoolInfo getById(Long schoolId);

}
