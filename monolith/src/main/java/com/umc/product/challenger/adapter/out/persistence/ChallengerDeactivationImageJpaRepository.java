package com.umc.product.challenger.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.umc.product.challenger.domain.ChallengerDeactivationImage;

public interface ChallengerDeactivationImageJpaRepository extends JpaRepository<ChallengerDeactivationImage, Long> {

    boolean existsByImageId(String imageId);
}
