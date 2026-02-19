package com.umc.product.challenger.adapter.out.persistence;

import com.umc.product.challenger.application.port.in.query.dto.SearchChallengerQuery;
import com.umc.product.challenger.application.port.out.LoadChallengerPort;
import com.umc.product.challenger.application.port.out.SaveChallengerPort;
import com.umc.product.challenger.application.port.out.SearchChallengerPort;
import com.umc.product.challenger.domain.Challenger;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;
import com.umc.product.common.domain.enums.ChallengerPart;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengerPersistenceAdapter implements LoadChallengerPort, SaveChallengerPort, SearchChallengerPort {

    private final ChallengerJpaRepository repository;
    private final ChallengerQueryRepository queryRepository;

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
    public List<Challenger> findByGisuIdIn(List<Long> gisuIds) {
        return repository.findByGisuIdIn(gisuIds);
    }

    @Override
    public Long countByIdIn(Set<Long> ids) {
        return repository.countByIdIn(ids);
    }

    @Override
    public Challenger findTopByMemberIdOrderByCreatedAtDesc(Long memberId) {
        return repository.findTopByMemberIdOrderByCreatedAtDesc(memberId)
            .orElseThrow(() -> new ChallengerDomainException(ChallengerErrorCode.CHALLENGER_NOT_FOUND));
    }

    @Override
    public List<Challenger> findByIdIn(Set<Long> ids) {
        return repository.findByIdIn(ids);
    }

    @Override
    public Page<Challenger> search(SearchChallengerQuery query, Pageable pageable) {
        return queryRepository.pagingSearch(query, pageable);
    }

    @Override
    public List<Challenger> cursorSearch(SearchChallengerQuery query, Long cursor, int size) {
        return queryRepository.cursorSearch(query, cursor, size);
    }

    @Override
    public Map<ChallengerPart, Long> countByPart(SearchChallengerQuery query) {
        return queryRepository.countByPart(query);
    }

    @Override
    public Map<Long, Double> sumPointsByChallengerIds(Set<Long> challengerIds) {
        return queryRepository.sumPointsByChallengerIds(challengerIds);
    }

    @Override
    public Challenger save(Challenger challenger) {
        return repository.save(challenger);
    }

    @Override
    public List<Challenger> saveAll(List<Challenger> challengers) {
        return repository.saveAll(challengers);
    }

    @Override
    public void delete(Challenger challenger) {
        repository.delete(challenger);
    }
}
