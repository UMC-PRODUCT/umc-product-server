package com.umc.product.challenger.adapter.out.persistence;

import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengerPersistenceAdapter implements LoadChallengerPort {

    private final ChallengerJpaRepository challengerJpaRepository;

    @Override
    public Optional<Challenger> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Challenger> findByGisuId(Long gisuId) {
        return null;
    }

    @Override
    public Challenger findByMemberIdAndGisuId(Long memberId, Long gisuId) {
        return challengerJpaRepository.findByMemberIdAndGisuId(memberId, gisuId)
                .orElseThrow(() -> new BusinessException(Domain.CHALLENGER, ChallengerErrorCode.CHALLENGER_NOT_FOUND));
    }
}
