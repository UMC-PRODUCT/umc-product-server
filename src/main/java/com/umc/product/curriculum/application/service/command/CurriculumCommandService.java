package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.CreateCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.EditCurriculumCommand;
import com.umc.product.curriculum.application.port.out.LoadCurriculumPort;
import com.umc.product.curriculum.application.port.out.LoadWeeklyCurriculumPort;
import com.umc.product.curriculum.application.port.out.SaveCurriculumPort;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CurriculumCommandService implements ManageCurriculumUseCase {

    private final LoadCurriculumPort loadCurriculumPort;
    private final SaveCurriculumPort saveCurriculumPort;
    private final LoadWeeklyCurriculumPort loadWeeklyCurriculumPort;

    // TODO: 기수(Gisu) 유효성 검사 필요 - organization 도메인의 GetGisuUseCase.getById() 호출로 존재 여부 확인
    //  현재 gisuId가 실제 존재하는 기수인지 검증하지 않음
    @Override
    public Long create(CreateCurriculumCommand command) {
        if (loadCurriculumPort.existsByGisuIdAndPart(command.gisuId(), command.part())) {
            throw new CurriculumDomainException(CurriculumErrorCode.CURRICULUM_ALREADY_EXISTS);
        }
        Curriculum curriculum = Curriculum.create(command.gisuId(), command.part(), command.title());
        return saveCurriculumPort.save(curriculum).getId();
    }

    @Override
    public void edit(EditCurriculumCommand command) {
        Curriculum curriculum = loadCurriculumPort.findById(command.curriculumId())
            .orElseThrow(() -> new CurriculumDomainException(CurriculumErrorCode.CURRICULUM_NOT_FOUND));
        curriculum.updateTitle(command.title());
        saveCurriculumPort.save(curriculum);
    }

    @Override
    public void delete(Long curriculumId) {
        Curriculum curriculum = loadCurriculumPort.findById(curriculumId)
            .orElseThrow(() -> new CurriculumDomainException(CurriculumErrorCode.CURRICULUM_NOT_FOUND));
        if (loadWeeklyCurriculumPort.existsByCurriculumId(curriculumId)) {
            throw new CurriculumDomainException(CurriculumErrorCode.CURRICULUM_HAS_WEEKLY_CURRICULUMS);
        }
        saveCurriculumPort.delete(curriculum);
    }
}