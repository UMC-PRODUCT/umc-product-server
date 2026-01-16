package com.umc.product.organization.adapter.out.persistence;


import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.domain.School;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchoolPersistenceAdapter implements ManageSchoolPort, LoadSchoolPort {

    private final SchoolJpaRepository schoolJpaRepository;
    private final SchoolQueryRepository schoolQueryRepository;

    public School save(School school) {
        return schoolJpaRepository.save(school);
    }

    @Override
    public Page<SchoolListItemInfo> getSchools(SchoolSearchCondition condition, Pageable pageable) {
        return schoolQueryRepository.getSchools(condition, pageable);
    }
}
