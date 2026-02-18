package com.umc.product.community.application.port.out.trophy;

import com.umc.product.community.application.port.in.query.dto.TrophySearchQuery;
import com.umc.product.community.domain.Trophy;
import java.util.List;
import java.util.Optional;

public interface LoadTrophyPort {
    Optional<Trophy> findById(Long trophyId);

    List<Trophy> findAllByQuery(TrophySearchQuery query);

    List<Trophy> findByChallengerId(Long challengerId);

    List<Trophy> findByWeek(Integer week);
}
