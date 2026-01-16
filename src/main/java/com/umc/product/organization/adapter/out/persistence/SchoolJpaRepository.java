package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolJpaRepository extends JpaRepository<School, Long> {
}
