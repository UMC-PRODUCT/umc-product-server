package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.Gisu;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadGisuPort {

    Gisu findActiveGisu();

    Optional<Gisu> findActiveGisuWithLock();

    Gisu findById(Long gisuId);

    List<Gisu> findByIds(Set<Long> gisuIds);

    List<Gisu> findAll();

    Page<Gisu> findAll(Pageable pageable);

    boolean existsByGeneration(Long generation);

    Optional<Gisu> findGisuByDate(Instant targetDate);
}
