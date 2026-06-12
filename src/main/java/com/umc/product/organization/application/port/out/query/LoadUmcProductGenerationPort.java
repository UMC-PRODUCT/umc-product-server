package com.umc.product.organization.application.port.out.query;

import com.umc.product.organization.domain.UmcProductGeneration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoadUmcProductGenerationPort {

    UmcProductGeneration getById(Long umcProductGenerationId);

    Optional<UmcProductGeneration> findById(Long umcProductGenerationId);

    Optional<UmcProductGeneration> findActiveWithLock();

    List<UmcProductGeneration> listByIds(Collection<Long> umcProductGenerationIds);

    List<UmcProductGeneration> findAll();

    boolean existsById(Long umcProductGenerationId);

    boolean existsByGeneration(Long generation);
}
