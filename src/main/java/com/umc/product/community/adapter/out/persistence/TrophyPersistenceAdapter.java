package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.application.port.in.trophy.query.TrophySearchQuery;
import com.umc.product.community.application.port.out.LoadTrophyPort;
import com.umc.product.community.application.port.out.SaveTrophyPort;
import com.umc.product.community.domain.Trophy;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrophyPersistenceAdapter implements LoadTrophyPort, SaveTrophyPort {

    private final TrophyRepository trophyRepository;

    @Override
    public Optional<Trophy> findById(Long trophyId) {
        return trophyRepository.findById(trophyId)
                .map(TrophyJpaEntity::toDomain);
    }

    @Override
    public List<Trophy> findAllByQuery(TrophySearchQuery query) {
        // TODO: QueryDSL을 사용한 동적 쿼리 필요 (week, school, part 필터링)
        // 현재는 week 기준으로만 조회
        if (query.week() != null) {
            return trophyRepository.findByWeekOrderByIdDesc(query.week()).stream()
                    .map(TrophyJpaEntity::toDomain)
                    .toList();
        }
        return trophyRepository.findAll().stream()
                .map(TrophyJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Trophy> findByChallengerId(Long challengerId) {
        return trophyRepository.findByChallengerId(challengerId).stream()
                .map(TrophyJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<Trophy> findByWeek(Integer week) {
        return trophyRepository.findByWeek(week).stream()
                .map(TrophyJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Trophy save(Trophy trophy) {
        TrophyJpaEntity entity = TrophyJpaEntity.from(trophy);
        TrophyJpaEntity saved = trophyRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public void delete(Trophy trophy) {
        if (trophy.getTrophyId() != null) {
            trophyRepository.deleteById(trophy.getTrophyId().id());
        }
    }

    @Override
    public void deleteById(Long trophyId) {
        trophyRepository.deleteById(trophyId);
    }
}
