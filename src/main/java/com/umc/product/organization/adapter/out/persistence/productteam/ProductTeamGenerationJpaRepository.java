package com.umc.product.organization.adapter.out.persistence.productteam;

import com.umc.product.organization.domain.ProductTeamGeneration;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ProductTeamGenerationJpaRepository extends JpaRepository<ProductTeamGeneration, Long> {

    List<ProductTeamGeneration> findAllByOrderByGenerationDesc();

    List<ProductTeamGeneration> findByIdIn(Collection<Long> ids);

    boolean existsByGeneration(Long generation);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM ProductTeamGeneration g WHERE g.isActive = true")
    Optional<ProductTeamGeneration> findActiveWithLock();
}
