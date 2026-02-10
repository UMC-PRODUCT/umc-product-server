package com.umc.product.challenger.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerPointUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerPointInfo;
import com.umc.product.challenger.application.port.out.LoadChallengerPointPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChallengerPointQueryService implements GetChallengerPointUseCase {

    private final LoadChallengerPointPort loadChallengerPointPort;

    @Override
    public List<ChallengerPointInfo> getListByChallengerId(Long challengerId) {
        return loadChallengerPointPort.findByChallengerId(challengerId)
            .stream().map(ChallengerPointInfo::from).toList();
    }
}
