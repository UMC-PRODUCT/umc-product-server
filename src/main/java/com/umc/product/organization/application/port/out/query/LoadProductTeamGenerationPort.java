package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.ProductTeamGeneration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoadProductTeamGenerationPort {

    ProductTeamGeneration getById(Long productTeamGenerationId);

    Optional<ProductTeamGeneration> findById(Long productTeamGenerationId);

    Optional<ProductTeamGeneration> findActiveWithLock();

    List<ProductTeamGeneration> listByIds(Collection<Long> productTeamGenerationIds);

    List<ProductTeamGeneration> findAll();

    boolean existsById(Long productTeamGenerationId);

    boolean existsByGeneration(Long generation);
}
