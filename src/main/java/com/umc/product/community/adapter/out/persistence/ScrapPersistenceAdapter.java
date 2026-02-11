package com.umc.product.community.adapter.out.persistence;

import com.umc.product.community.application.port.out.LoadScrapPort;
import com.umc.product.community.application.port.out.SaveScrapPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ScrapPersistenceAdapter implements LoadScrapPort, SaveScrapPort {

    private final ScrapRepository scrapRepository;

    @Override
    public boolean existsByPostIdAndChallengerId(Long postId, Long challengerId) {
        return scrapRepository.existsByPostIdAndChallengerId(postId, challengerId);
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
