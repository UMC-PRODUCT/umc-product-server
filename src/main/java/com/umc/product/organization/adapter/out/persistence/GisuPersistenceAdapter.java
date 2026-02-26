package com.umc.product.organization.adapter.out.persistence;


import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GisuPersistenceAdapter implements ManageGisuPort, LoadGisuPort {

    private final GisuJpaRepository gisuJpaRepository;

    public Gisu findActiveGisu() {
        return gisuJpaRepository.findByIsActiveTrue().orElseThrow(
                () -> new BusinessException(Domain.ORGANIZATION, OrganizationErrorCode.GISU_IS_ACTIVE_NOT_FOUND));
    }

    @Override
    public Gisu findById(Long gisuId) {
        return gisuJpaRepository.findById(gisuId).orElseThrow(
                () -> new BusinessException(Domain.ORGANIZATION, OrganizationErrorCode.GISU_NOT_FOUND));
    }

    @Override
    public List<Gisu> findByIds(Set<Long> gisuIds) {
        return gisuJpaRepository.findByIdIn(gisuIds);
    }

    @Override
    public List<Gisu> findAll() {
        return gisuJpaRepository.findAllByOrderByGenerationDesc();
    }

    @Override
    public Page<Gisu> findAll(Pageable pageable) {
        return gisuJpaRepository.findAllByOrderByGenerationDesc(pageable);
    }

    @Override
    public Gisu save(Gisu gisu) {
        return gisuJpaRepository.save(gisu);
    }

    @Override
    public boolean existsByGeneration(Long generation) {
        return gisuJpaRepository.existsByGeneration(generation);
    }

    @Override
    public void delete(Gisu gisu) {
        gisuJpaRepository.delete(gisu);
    }
}
