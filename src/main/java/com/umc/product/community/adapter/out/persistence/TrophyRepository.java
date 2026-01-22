package com.umc.product.community.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrophyRepository extends JpaRepository<TrophyJpaEntity, Long> {

    List<TrophyJpaEntity> findByChallengerId(Long challengerId);

    List<TrophyJpaEntity> findByWeek(Integer week);

    List<TrophyJpaEntity> findByWeekOrderByIdDesc(Integer week);
}
