package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumProjection;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookProgressProjection;
import com.umc.product.curriculum.application.port.out.LoadCurriculumPort;
import com.umc.product.curriculum.application.port.out.LoadCurriculumProgressPort;
import com.umc.product.curriculum.application.port.out.SaveCurriculumPort;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurriculumPersistenceAdapter implements LoadCurriculumPort, LoadCurriculumProgressPort,
    SaveCurriculumPort {

    private final CurriculumJpaRepository curriculumJpaRepository;
    private final CurriculumQueryRepository curriculumQueryRepository;

    @Override
    public Optional<Curriculum> findById(Long id) {
        return curriculumJpaRepository.findById(id);
    }

    @Override
    public Optional<CurriculumProjection> findByGisuIdAndPart(Long gisuId, ChallengerPart part) {
        return curriculumQueryRepository.findByGisuIdAndPart(gisuId, part);
    }

    @Override
    public CurriculumProjection getByGisuIdAndPart(Long gisuId, ChallengerPart part) {
        return findByGisuIdAndPart(gisuId, part)
            .orElseThrow(() -> new CurriculumDomainException(CurriculumErrorCode.CURRICULUM_NOT_FOUND));
    }

    @Override
    public boolean existsById(Long id) {
        return curriculumJpaRepository.existsById(id);
    }

    @Override
    public boolean existsByGisuIdAndPart(Long gisuId, ChallengerPart part) {
        return curriculumJpaRepository.existsByGisuIdAndPart(gisuId, part);
    }

    @Override
    public List<WorkbookProgressProjection> findWorkbookProgressProjections(Long curriculumId, Long challengerId) {
        return curriculumQueryRepository.findWorkbookProgressProjections(curriculumId, challengerId);
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
