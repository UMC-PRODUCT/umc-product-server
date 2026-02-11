package com.umc.product.community.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapRepository extends JpaRepository<ScrapJpaEntity, Long> {

    Optional<ScrapJpaEntity> findByPostIdAndChallengerId(Long postId, Long challengerId);

    boolean existsByPostIdAndChallengerId(Long postId, Long challengerId);

    int countByPostId(Long postId);

    void deleteByPostIdAndChallengerId(Long postId, Long challengerId);
}
