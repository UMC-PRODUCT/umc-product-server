package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerPointInfo;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GetChallengerPointUseCase {
    List<ChallengerPointInfo> getListByChallengerId(Long challengerId);

    /**
     * 여러 챌린저의 포인트를 한 번의 쿼리로 일괄 조회
     *
     * @return challengerId → List<ChallengerPointInfo> 맵
     */
    Map<Long, List<ChallengerPointInfo>> getMapByChallengerIds(Set<Long> challengerIds);
}
