package com.umc.product.organization.adapter.out.persistence.umcproduct;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.umc.product.organization.domain.UmcProductGeneration;

import jakarta.persistence.LockModeType;

public interface UmcProductGenerationJpaRepository extends JpaRepository<UmcProductGeneration, Long> {

    List<UmcProductGeneration> findAllByOrderByGenerationDesc();

    List<UmcProductGeneration> findByIdIn(Collection<Long> ids);

    boolean existsByGeneration(Long generation);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM UmcProductGeneration g WHERE g.isActive = true")
    Optional<UmcProductGeneration> findActiveWithLock();
}
