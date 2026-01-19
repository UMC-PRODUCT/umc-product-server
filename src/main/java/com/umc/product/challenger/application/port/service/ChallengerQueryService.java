package com.umc.product.challenger.application.port.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengerQueryService implements GetChallengerUseCase {

    private final LoadChallengerPort loadChallengerPort;

    @Override
    public ChallengerInfo getChallengerPublicInfo(Long challengerId) {
        return null;
    }

    @Override
    public List<ChallengerInfo> getMemberChallengerList(Long memberId) {
        return null;
    }

    @Override
    public ChallengerInfo getMemberGisuChallengerInfo(Long memberId, Long gisuId) {
        return ChallengerInfo.from(loadChallengerPort.findByMemberIdAndGisuId(memberId, gisuId));
    }
}
