package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.application.port.out.LoadScrapPort;
import com.umc.product.community.application.port.out.SaveScrapPort;
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
}
