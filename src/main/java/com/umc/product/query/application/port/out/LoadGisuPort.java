package com.umc.product.query.application.port.out;

import com.umc.product.command.organization.domain.Gisu;
import java.util.List;
import java.util.Optional;

public interface LoadGisuPort {

    Optional<Gisu> findById(Long id);

    List<Gisu> findAll();
}
