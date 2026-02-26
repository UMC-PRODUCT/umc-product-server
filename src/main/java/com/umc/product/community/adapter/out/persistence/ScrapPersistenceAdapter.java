package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.adapter.out.persistence.entity.ScrapJpaEntity;
import com.umc.product.community.application.port.out.scrap.LoadScrapPort;
import com.umc.product.community.application.port.out.scrap.SaveScrapPort;
import com.umc.product.community.domain.Scrap;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ScrapPersistenceAdapter implements LoadScrapPort, SaveScrapPort {

    private final ScrapRepository scrapRepository;

    @Override
    public Optional<Scrap> findByPostIdAndChallengerId(Long postId, Long challengerId) {
        return scrapRepository.findByPostIdAndChallengerId(postId, challengerId)
            .map(ScrapJpaEntity::toDomain);
    }

    @Override
    public boolean existsByPostIdAndChallengerId(Long postId, Long challengerId) {
        return scrapRepository.existsByPostIdAndChallengerId(postId, challengerId);
    }

    @Override
    public int countByPostId(Long postId) {
        return scrapRepository.countByPostId(postId);
    }

    @Override
    public Scrap save(Scrap scrap) {
        ScrapJpaEntity entity = ScrapJpaEntity.from(scrap);
        ScrapJpaEntity saved = scrapRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public void delete(Scrap scrap) {
        if (scrap.getScrapId() != null) {
            scrapRepository.deleteById(scrap.getScrapId().id());
        }
    }

    @Override
    @Transactional
    public void deleteByPostIdAndChallengerId(Long postId, Long challengerId) {
        scrapRepository.deleteByPostIdAndChallengerId(postId, challengerId);
    }

    @Override
    @Transactional
    public boolean toggleScrap(Long postId, Long challengerId) {
        Optional<ScrapJpaEntity> existing = scrapRepository.findByPostIdAndChallengerId(postId, challengerId);

        if (existing.isPresent()) {
            // 스크랩 취소
            scrapRepository.delete(existing.get());
            return false;
        } else {
            // 스크랩 추가
            ScrapJpaEntity scrap = ScrapJpaEntity.of(postId, challengerId);
            scrapRepository.save(scrap);
            return true;
        }
    }
}
