package com.umc.product.challenger.adapter.out.persistence;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.umc.product.challenger.application.port.out.LoadChallengerDeactivationPort;
import com.umc.product.challenger.application.port.out.SaveChallengerDeactivationPort;
import com.umc.product.challenger.domain.ChallengerDeactivation;
import com.umc.product.challenger.domain.ChallengerDeactivationHistory;
import com.umc.product.challenger.domain.ChallengerDeactivationImage;
import com.umc.product.challenger.domain.ChallengerDeactivationLink;
import com.umc.product.challenger.domain.exception.ChallengerDomainException;
import com.umc.product.challenger.domain.exception.ChallengerErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ChallengerDeactivationPersistenceAdapter
    implements LoadChallengerDeactivationPort, SaveChallengerDeactivationPort {

    private final ChallengerDeactivationJpaRepository deactivationRepository;
    private final ChallengerDeactivationHistoryJpaRepository historyRepository;
    private final ChallengerDeactivationImageJpaRepository imageRepository;
    private final ChallengerDeactivationLinkJpaRepository linkRepository;

    @Override
    public boolean existsByChallengerId(Long challengerId) {
        return deactivationRepository.existsByChallengerId(challengerId);
    }

    @Override
    public ChallengerDeactivation save(ChallengerDeactivation deactivation) {
        try {
            return deactivationRepository.saveAndFlush(deactivation);
        } catch (DataIntegrityViolationException exception) {
            throw new ChallengerDomainException(
                ChallengerErrorCode.CHALLENGER_DEACTIVATION_ALREADY_EXISTS,
                exception
            );
        }
    }

    @Override
    public ChallengerDeactivationHistory saveHistory(ChallengerDeactivationHistory history) {
        return historyRepository.save(history);
    }

    @Override
    public void saveImages(List<ChallengerDeactivationImage> images) {
        imageRepository.saveAll(images);
    }

    @Override
    public void saveLinks(List<ChallengerDeactivationLink> links) {
        linkRepository.saveAll(links);
    }
}
