package com.umc.product.challenger.adapter.out.persistence;

import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengerPersistenceAdapter implements LoadChallengerPort, SaveChallengerPort {

    private final ChallengerJpaRepository repository;

    @Override
    public Optional<Challenger> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Challenger getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_FOUND));
    }

    @Override
    public Optional<Challenger> findByMemberIdAndGisuId(Long memberId, Long gisuId) {
        return repository.findByMemberIdAndGisuId(memberId, gisuId);
    }

    @Override
    public List<Challenger> findByMemberId(Long memberId) {
        return repository.findByMemberId(memberId);
    }

    @Override
    public List<Challenger> findByGisuId(Long gisuId) {
        return repository.findByGisuId(gisuId);
    }

    @Override
    public Challenger save(Challenger challenger) {
        return repository.save(challenger);
    }

    @Override
    public void delete(Challenger challenger) {
        repository.delete(challenger);
    }
}
