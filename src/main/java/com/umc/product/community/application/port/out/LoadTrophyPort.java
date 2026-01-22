package com.umc.product.community.application.port.out;

import com.umc.product.community.application.port.in.trophy.query.TrophySearchQuery;
import com.umc.product.community.domain.Trophy;
import java.util.List;
import java.util.Optional;

public interface LoadTrophyPort {
    Optional<Trophy> findById(Long trophyId);

    List<Trophy> findAllByQuery(TrophySearchQuery query);

    List<Trophy> findByChallengerId(Long challengerId);

    List<Trophy> findByWeek(Integer week);
}
