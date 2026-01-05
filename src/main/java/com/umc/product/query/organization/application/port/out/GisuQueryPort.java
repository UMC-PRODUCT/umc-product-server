package com.umc.product.query.organization.application.port.out;

import com.umc.product.command.organization.domain.Gisu;
import java.util.List;
import java.util.Optional;

public interface GisuQueryPort {

    Optional<Gisu> findById(Long id);

    List<Gisu> findAll();
}
