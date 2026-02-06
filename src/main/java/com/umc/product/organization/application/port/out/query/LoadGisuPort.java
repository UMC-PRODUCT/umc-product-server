package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.Gisu;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoadGisuPort {

    Gisu findActiveGisu();

    Gisu findById(Long gisuId);

    List<Gisu> findAll();

    Page<Gisu> findAll(Pageable pageable);

    boolean existsByGeneration(Long generation);
}
