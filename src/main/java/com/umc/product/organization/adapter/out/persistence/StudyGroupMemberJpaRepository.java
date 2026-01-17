package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.StudyGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyGroupMemberJpaRepository extends JpaRepository<StudyGroupMember, Long> {

}
