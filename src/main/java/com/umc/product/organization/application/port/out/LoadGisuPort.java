package com.umc.product.organization.application.port.out;

import com.umc.product.organization.domain.Gisu;
import java.util.List;
import java.util.Optional;

public interface LoadGisuPort {

    Optional<Gisu> findById(Long id);

    List<Gisu> findAll();
}
