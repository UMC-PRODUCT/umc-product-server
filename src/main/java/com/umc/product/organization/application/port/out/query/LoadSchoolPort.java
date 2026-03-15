package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.application.port.in.query.dto.SchoolChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolNameInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import com.umc.product.organization.domain.School;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadSchoolPort {

    Page<SchoolListItemInfo> findSchools(SchoolSearchCondition condition, Pageable pageable);

    List<SchoolNameInfo> findAllNames();

    School findSchoolDetailById(Long schoolId);

    School findById(Long schoolId);

    SchoolChapterInfo findSchoolDetailByIdWithActiveChapter(Long schoolId);

    List<SchoolDetailInfo.SchoolLinkItem> findLinksBySchoolId(Long schoolId);

    Map<Long, List<SchoolDetailInfo.SchoolLinkItem>> findLinksBySchoolIds(List<Long> schoolIds);

    List<SchoolChapterInfo> findSchoolDetailsByGisuId(Long gisuId);

    List<School> findAllByIds(List<Long> schoolIds);

    List<School> findUnassignedByGisuId(Long gisuId);

    boolean existsById(Long schoolId);

    void throwIfNotExists(Long schoolId);
}
