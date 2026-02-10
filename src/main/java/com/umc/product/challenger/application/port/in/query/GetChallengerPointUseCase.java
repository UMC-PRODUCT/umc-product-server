package com.umc.product.challenger.application.port.in.query;

import com.umc.product.challenger.application.port.in.query.dto.ChallengerPointInfo;
import java.util.List;

public interface GetChallengerPointUseCase {
    List<ChallengerPointInfo> getListByChallengerId(Long challengerId);
}
