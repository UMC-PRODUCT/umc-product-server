package com.umc.product.organization.adapter.out.persistence;


import com.umc.product.organization.domain.CentralOrganization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CentralOrganizationJpaRepository extends JpaRepository<CentralOrganization, Long> {

}
