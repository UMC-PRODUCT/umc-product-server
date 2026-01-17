package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.Gisu;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface GisuJpaRepository extends Repository<Gisu, Long> {

    Optional<Gisu> findByIsActiveTrue();

    Optional<Gisu> findById(Long id);

    List<Gisu> findAllByOrderByGenerationDesc();

    Gisu save(Gisu gisu);
}
