package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.ManageWeeklyCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.CreateWeeklyCurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.curriculum.EditWeeklyCurriculumCommand;
import com.umc.product.curriculum.application.port.out.LoadCurriculumPort;
import com.umc.product.curriculum.application.port.out.LoadWeeklyCurriculumPort;
import com.umc.product.curriculum.application.port.out.SaveWeeklyCurriculumPort;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.WeeklyCurriculum;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class WeeklyCurriculumCommandService implements ManageWeeklyCurriculumUseCase {

    private final LoadCurriculumPort loadCurriculumPort;
    private final LoadWeeklyCurriculumPort loadWeeklyCurriculumPort;
    private final SaveWeeklyCurriculumPort saveWeeklyCurriculumPort;

    @Override
    public Long create(CreateWeeklyCurriculumCommand command) {
        Curriculum curriculum = loadCurriculumPort.findById(command.curriculumId())
            .orElseThrow(() -> new CurriculumDomainException(CurriculumErrorCode.CURRICULUM_NOT_FOUND));

        if (command.endsAt().isBefore(Instant.now())) {
            throw new CurriculumDomainException(CurriculumErrorCode.WEEKLY_CURRICULUM_PERIOD_ALREADY_ENDED);
        }

        // 해당 주차별 커리큘럼에 이미 같은 타입의 커리큘럼이 존재하는지 확인
        if (loadWeeklyCurriculumPort.existsByCurriculumIdAndWeekNoAndIsExtra(
            command.curriculumId(), command.weekNo(), command.isExtra())) {
            throw new CurriculumDomainException(CurriculumErrorCode.WEEKLY_CURRICULUM_ALREADY_EXISTS);
        }

        WeeklyCurriculum weeklyCurriculum = WeeklyCurriculum.create(
            curriculum,
            command.weekNo(),
            command.isExtra(),
            command.title(),
            command.startsAt(),
            command.endsAt()
        );
        return saveWeeklyCurriculumPort.save(weeklyCurriculum).getId();
    }

    @Override
    public void edit(EditWeeklyCurriculumCommand command) {
        WeeklyCurriculum weeklyCurriculum = loadWeeklyCurriculumPort.getById(command.weeklyCurriculumId());

        if (command.endsAt() != null && command.endsAt().isBefore(Instant.now())) {
            throw new CurriculumDomainException(CurriculumErrorCode.WEEKLY_CURRICULUM_PERIOD_ALREADY_ENDED);
        }

        // 시작/종료일 변경 요청이 있는 경우 배포된 워크북 존재 여부 확인
        if ((command.startsAt() != null || command.endsAt() != null)
            && loadWeeklyCurriculumPort.existsReleasedOriginalWorkbookByWeeklyCurriculumId(
            command.weeklyCurriculumId())) {
            throw new CurriculumDomainException(CurriculumErrorCode.WEEKLY_CURRICULUM_DATE_LOCKED);
        }

        // 주차 번호 또는 부록 여부 변경 시 중복 검사
        if (command.weekNo() != null || command.isExtra() != null) {
            long effectiveWeekNo = command.weekNo() != null ? command.weekNo() : weeklyCurriculum.getWeekNo();
            boolean effectiveIsExtra = command.isExtra() != null ? command.isExtra() : weeklyCurriculum.isExtra();
            if (loadWeeklyCurriculumPort.existsByCurriculumIdAndWeekNoAndIsExtraAndIdNot(
                weeklyCurriculum.getCurriculum().getId(), effectiveWeekNo, effectiveIsExtra, command.weeklyCurriculumId())) {
                throw new CurriculumDomainException(CurriculumErrorCode.WEEKLY_CURRICULUM_ALREADY_EXISTS);
            }
        }

        weeklyCurriculum.update(command.weekNo(), command.isExtra(), command.title(), command.startsAt(), command.endsAt());

        saveWeeklyCurriculumPort.save(weeklyCurriculum);
    }

    @Override
    public void delete(Long weeklyCurriculumId) {
        WeeklyCurriculum weeklyCurriculum = loadWeeklyCurriculumPort.getById(weeklyCurriculumId);
        if (loadWeeklyCurriculumPort.existsOriginalWorkbookByWeeklyCurriculumId(weeklyCurriculumId)) {
            throw new CurriculumDomainException(CurriculumErrorCode.WEEKLY_CURRICULUM_HAS_WORKBOOKS);
        }
        saveWeeklyCurriculumPort.delete(weeklyCurriculum);
    }
}
