package com.umc.product.organization.adapter.out.persistence.school;


import com.umc.product.organization.application.port.in.query.dto.SchoolChapterInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolDetailInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolNameInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.domain.School;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchoolPersistenceAdapter implements ManageSchoolPort, LoadSchoolPort {

    private final SchoolJpaRepository schoolJpaRepository;
    private final SchoolLinkJpaRepository schoolLinkJpaRepository;
    private final SchoolQueryRepository schoolQueryRepository;

    @Override
    public School save(School school) {
        return schoolJpaRepository.save(school);
    }

    @Override
    public void deleteAllByIds(List<Long> schoolIds) {
        schoolJpaRepository.deleteAllByIdIn(schoolIds);
    }

    @Override
    public void deleteAllLinksBySchoolIds(List<Long> schoolIds) {
        schoolLinkJpaRepository.deleteAllBySchoolIdIn(schoolIds);
    }

    @Override
    public Page<SchoolListItemInfo> findSchools(SchoolSearchCondition condition, Pageable pageable) {
        return schoolQueryRepository.getSchools(condition, pageable);
    }

    @Override
    public List<SchoolNameInfo> findAllNames() {
        return schoolQueryRepository.findAllNames();
    }

    @Override
    public School findSchoolDetailById(Long schoolId) {
        return schoolJpaRepository.findByIdWithDetails(schoolId)
            .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.SCHOOL_NOT_FOUND));
    }

    @Override
    public School findById(Long schoolId) {
        return schoolJpaRepository.findById(schoolId)
            .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.SCHOOL_NOT_FOUND));
    }

    @Override
    public List<School> findAllByIds(List<Long> schoolIds) {
        return schoolJpaRepository.findAllByIdIn(schoolIds);
    }

    public boolean existsById(Long schoolId) {
        return schoolJpaRepository.existsById(schoolId);
    }

    @Override
    public void throwIfNotExists(Long schoolId) {
        if (!existsById(schoolId)) {
            throw new OrganizationDomainException(OrganizationErrorCode.SCHOOL_NOT_FOUND);
        }
    }

    @Override
    public List<School> findUnassignedByGisuId(Long gisuId) {
        return schoolJpaRepository.findUnassignedByGisuId(gisuId);
    }

    @Override
    public List<SchoolChapterInfo> findSchoolDetailsByGisuId(Long gisuId) {
        return schoolQueryRepository.getSchoolDetailsByGisuId(gisuId);
    }

    @Override
    public Map<Long, List<SchoolDetailInfo.SchoolLinkItem>> findLinksBySchoolIds(List<Long> schoolIds) {
        return schoolQueryRepository.findLinksBySchoolIds(schoolIds);
    }

    @Override
    public SchoolChapterInfo findSchoolDetailByIdWithActiveChapter(Long schoolId) {
        SchoolChapterInfo schoolInfoWithoutSchoolLinkItem = schoolQueryRepository.getSchoolDetail(schoolId);
        if (schoolInfoWithoutSchoolLinkItem == null) {
            throw new OrganizationDomainException(OrganizationErrorCode.SCHOOL_NOT_FOUND);
        }
        return schoolInfoWithoutSchoolLinkItem;
    }

    @Override
    public List<SchoolDetailInfo.SchoolLinkItem> findLinksBySchoolId(Long schoolId) {
        return schoolQueryRepository.findLinksBySchoolId(schoolId);
    }
}
