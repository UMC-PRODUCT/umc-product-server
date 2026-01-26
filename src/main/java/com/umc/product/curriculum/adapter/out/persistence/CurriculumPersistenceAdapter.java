package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.CurriculumProgressInfo;
import com.umc.product.curriculum.application.port.out.LoadCurriculumPort;
import com.umc.product.curriculum.application.port.out.LoadCurriculumProgressPort;
import com.umc.product.curriculum.application.port.out.SaveCurriculumPort;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurriculumPersistenceAdapter implements LoadCurriculumPort, LoadCurriculumProgressPort, SaveCurriculumPort {

    private final CurriculumJpaRepository curriculumJpaRepository;
    private final CurriculumQueryRepository curriculumQueryRepository;

    @Override
    public Optional<Curriculum> findById(Long id) {
        return curriculumJpaRepository.findById(id);
    }

    @Override
    public Optional<Curriculum> findByActiveGisuAndPart(ChallengerPart part) {
        return curriculumJpaRepository.findByActiveGisuAndPart(part);
    }

    @Override
    public boolean existsById(Long id) {
        return curriculumJpaRepository.existsById(id);
    }

    @Override
    public CurriculumProgressInfo findCurriculumProgress(Long challengerId, ChallengerPart part) {
        return curriculumQueryRepository.findCurriculumProgress(challengerId, part)
                .orElseThrow(() -> new BusinessException(Domain.CURRICULUM, CurriculumErrorCode.CURRICULUM_NOT_FOUND));
    }

    @Override
    public Curriculum save(Curriculum curriculum) {
        return curriculumJpaRepository.save(curriculum);
    }

    @Override
    public void delete(Curriculum curriculum) {
        curriculumJpaRepository.delete(curriculum);
    }
}
