package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.School;
import org.springframework.data.repository.Repository;

public interface SchoolJpaRepository extends Repository<School, Long> {

    School save(School school);
}
