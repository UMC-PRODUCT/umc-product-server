package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupJpaRepository extends JpaRepository<StudyGroup, Long> {
}
