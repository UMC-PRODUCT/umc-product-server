package com.umc.product.organization.application.port.in;


import com.umc.product.organization.application.port.in.dto.SchoolDetailQuery;
import com.umc.product.organization.application.port.in.dto.SchoolOptionQuery;
import java.util.List;

public interface GetSchoolUserCase {
    List<SchoolOptionQuery> getAll(Long gisuId);
    SchoolDetailQuery getById(Long schoolId);

}
