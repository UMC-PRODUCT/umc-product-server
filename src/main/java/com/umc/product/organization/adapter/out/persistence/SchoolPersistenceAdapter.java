package com.umc.product.organization.adapter.out.persistence;


import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.domain.School;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchoolPersistenceAdapter implements ManageSchoolPort, LoadSchoolPort {

    private final SchoolJpaRepository schoolJpaRepository;
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
    public Page<SchoolListItemInfo> findSchools(SchoolSearchCondition condition, Pageable pageable) {
        return schoolQueryRepository.getSchools(condition, pageable);
    }

    @Override
    public School findSchoolDetailById(Long schoolId) {
        return schoolJpaRepository.findByIdWithDetails(schoolId)
                .orElseThrow(() -> new BusinessException(Domain.ORGANIZATION, OrganizationErrorCode.SCHOOL_NOT_FOUND));
    }

    @Override
    public School findById(Long schoolId) {
        return schoolJpaRepository.findById(schoolId)
                .orElseThrow(() -> new BusinessException(Domain.ORGANIZATION, OrganizationErrorCode.SCHOOL_NOT_FOUND));
    }

    @Override
    public List<School> findAllByIds(List<Long> schoolIds) {
        return schoolJpaRepository.findAllByIdIn(schoolIds);
    }
}
