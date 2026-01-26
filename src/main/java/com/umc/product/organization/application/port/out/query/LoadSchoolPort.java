package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import com.umc.product.organization.domain.School;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadSchoolPort {

    Page<SchoolListItemInfo> findSchools(SchoolSearchCondition condition, Pageable pageable);

//    void validateExistsById(Long schoolId);

    School findSchoolDetailById(Long schoolId);

    School findById(Long schoolId);

    List<School> findAllByIds(List<Long> schoolIds);

    List<School> findUnassignedByGisuId(Long gisuId);

    boolean existsById(Long schoolId);
}
