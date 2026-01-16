package com.umc.product.organization.adapter.out.persistence;


import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.application.port.out.query.LoadSchoolPort;
import com.umc.product.organization.domain.School;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SchoolPersistenceAdapter implements ManageSchoolPort, LoadSchoolPort {

    private final SchoolJpaRepository schoolJpaRepository;

    public School save(School school) {
        return schoolJpaRepository.save(school);
    }
}
