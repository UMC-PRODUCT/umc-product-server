package com.umc.product.organization.adapter.out.persistence.umcproduct;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.umc.product.organization.application.port.out.command.SaveUmcProductGenerationPort;
import com.umc.product.organization.application.port.out.query.LoadUmcProductGenerationPort;
import com.umc.product.organization.domain.UmcProductGeneration;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UmcProductGenerationPersistenceAdapter
    implements LoadUmcProductGenerationPort, SaveUmcProductGenerationPort {

    private final UmcProductGenerationJpaRepository umcProductGenerationJpaRepository;

    @Override
    public UmcProductGeneration getById(Long umcProductGenerationId) {
        return findById(umcProductGenerationId)
            .orElseThrow(() -> new OrganizationDomainException(OrganizationErrorCode.UMC_PRODUCT_GENERATION_NOT_FOUND));
    }

    @Override
    public Optional<UmcProductGeneration> findById(Long umcProductGenerationId) {
        return umcProductGenerationJpaRepository.findById(umcProductGenerationId);
    }

    @Override
    public Optional<UmcProductGeneration> findActiveWithLock() {
        return umcProductGenerationJpaRepository.findActiveWithLock();
    }

    @Override
    public List<UmcProductGeneration> listByIds(Collection<Long> umcProductGenerationIds) {
        if (umcProductGenerationIds == null || umcProductGenerationIds.isEmpty()) {
            return List.of();
        }
        return umcProductGenerationJpaRepository.findByIdIn(umcProductGenerationIds);
    }

    @Override
    public List<UmcProductGeneration> findAll() {
        return umcProductGenerationJpaRepository.findAllByOrderByGenerationDesc();
    }

    @Override
    public boolean existsById(Long umcProductGenerationId) {
        return umcProductGenerationJpaRepository.existsById(umcProductGenerationId);
    }

    @Override
    public boolean existsByGeneration(Long generation) {
        return umcProductGenerationJpaRepository.existsByGeneration(generation);
    }

    @Override
    public UmcProductGeneration save(UmcProductGeneration generation) {
        return umcProductGenerationJpaRepository.save(generation);
    }

    @Override
    public void delete(UmcProductGeneration generation) {
        umcProductGenerationJpaRepository.delete(generation);
    }
}
