package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.Gisu;
import java.util.List;

public interface LoadGisuPort {

    Gisu findActiveGisu();

    Gisu findById(Long gisuId);

    List<Gisu> findAll();

    boolean existsByGeneration(Long generation);
}
