package com.umc.product.organization.adapter.out.persistence;


import com.umc.product.organization.application.port.in.query.dto.SchoolListItemInfo;
import com.umc.product.organization.application.port.in.query.dto.SchoolSearchCondition;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.domain.School;
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
    public Page<SchoolListItemInfo> getSchools(SchoolSearchCondition condition, Pageable pageable) {
        return schoolQueryRepository.getSchools(condition, pageable);
    }
}
