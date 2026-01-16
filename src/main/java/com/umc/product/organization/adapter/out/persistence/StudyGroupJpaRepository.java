package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.Gisu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupJpaRepository extends JpaRepository<Gisu, Long> {
}
