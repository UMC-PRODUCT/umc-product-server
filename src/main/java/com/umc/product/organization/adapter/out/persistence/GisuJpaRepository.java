package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.Gisu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GisuJpaRepository extends JpaRepository<Gisu, Long> {
}
