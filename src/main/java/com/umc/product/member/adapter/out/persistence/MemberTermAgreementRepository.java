package com.umc.product.member.adapter.out.persistence;

import com.umc.product.member.domain.MemberTermAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTermAgreementRepository extends JpaRepository<MemberTermAgreement, Long> {
}
