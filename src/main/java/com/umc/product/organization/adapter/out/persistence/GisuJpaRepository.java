package com.umc.product.organization.adapter.out.persistence;

import com.umc.product.organization.domain.Gisu;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface GisuJpaRepository extends Repository<Gisu, Long> {

    @Query("SELECT g FROM Gisu g WHERE g.isActive = true")
    Optional<Gisu> findByIsActiveTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Gisu g WHERE g.isActive = true")
    Optional<Gisu> findActiveWithLock();

    Optional<Gisu> findById(Long id);

    List<Gisu> findAllByOrderByGenerationDesc();

    Page<Gisu> findAllByOrderByGenerationDesc(Pageable pageable);

    List<Gisu> findByIdIn(Collection<Long> ids);

    Gisu save(Gisu gisu);

    boolean existsByGeneration(Long generation);

    boolean existsById(Long id);

    void delete(Gisu gisu);
}
