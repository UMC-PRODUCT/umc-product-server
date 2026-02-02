package com.umc.product.challenger.application.service;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
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
        Challenger challenger = loadChallengerPort.getById(challengerId);
        return ChallengerInfo.from(challenger);
    }

    @Override
    public ChallengerInfo getByMemberIdAndGisuId(Long memberId, Long gisuId) {
        Challenger challenger = loadChallengerPort.findByMemberIdAndGisuId(memberId, gisuId)
            .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_FOUND));
        return ChallengerInfo.from(challenger);
    }

    @Override
    public List<ChallengerInfo> getMemberChallengerList(Long memberId) {
        List<Challenger> challengers = loadChallengerPort.findByMemberId(memberId);
        return challengers.stream()
            .map(ChallengerInfo::from)
            .toList();
    }

    @Override
    public ChallengerInfo getLatestByMemberId(Long memberId) {
        return ChallengerInfo.from(loadChallengerPort.findTopByMemberIdOrderByCreatedAtDesc(memberId));
    }
}
