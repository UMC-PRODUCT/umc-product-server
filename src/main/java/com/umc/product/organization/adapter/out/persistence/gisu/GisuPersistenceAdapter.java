package com.umc.product.organization.adapter.out.persistence.gisu;


import com.umc.product.organization.application.port.out.command.SaveGisuPort;
import com.umc.product.organization.application.port.out.query.LoadGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.exception.OrganizationDomainException;
import com.umc.product.organization.exception.OrganizationErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GisuPersistenceAdapter implements SaveGisuPort, LoadGisuPort {

    private final GisuJpaRepository gisuJpaRepository;
    private final GisuQueryRepository gisuQueryRepository;

    public Gisu findActiveGisu() {
        return gisuJpaRepository.findByIsActiveTrue().orElseThrow(
            () -> new OrganizationDomainException(OrganizationErrorCode.GISU_IS_ACTIVE_NOT_FOUND));
    }

    @Override
    public Optional<Gisu> findActiveGisuWithLock() {
        return gisuJpaRepository.findActiveWithLock();
    }

    @Override
    public Gisu findById(Long gisuId) {
        return gisuJpaRepository.findById(gisuId).orElseThrow(
            () -> new OrganizationDomainException(OrganizationErrorCode.GISU_NOT_FOUND));
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
    public Optional<Gisu> findGisuByDate(Instant targetDate) {
        return gisuQueryRepository.findGisuByDate(targetDate);
    }

    @Override
    public void delete(Gisu gisu) {
        gisuJpaRepository.delete(gisu);
    }
}
