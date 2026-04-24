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

        WeeklyCurriculum weeklyCurriculum = WeeklyCurriculum.create(
            curriculum,
            command.weekNo(),
            command.isExtra(),
            command.title(),
            command.startsAt(),
            command.endsAt()
        );
        return saveWeeklyCurriculumPort.save(weeklyCurriculum).getId();
        // TODO: (curriculum_id, week_no, is_extra) UNIQUE 제약 위반 시 DataIntegrityViolationException 발생.
        //  현재 커스텀 예외로 변환하지 않음. GlobalExceptionHandler에서 처리하거나 여기서 사전 중복 검사 추가 필요.
    }

    @Override
    public void edit(EditWeeklyCurriculumCommand command) {
        WeeklyCurriculum weeklyCurriculum = loadWeeklyCurriculumPort.getById(command.weeklyCurriculumId());

        // 시작/종료일 변경 요청이 있는 경우 배포된 워크북 존재 여부 확인
        if ((command.startsAt() != null || command.endsAt() != null)
            && loadWeeklyCurriculumPort.existsReleasedOriginalWorkbookByWeeklyCurriculumId(
            command.weeklyCurriculumId())) {
            throw new CurriculumDomainException(CurriculumErrorCode.WEEKLY_CURRICULUM_DATE_LOCKED);
        }

        weeklyCurriculum.update(command.weekNo(), command.isExtra(), command.title(), command.startsAt(), command.endsAt());

        saveWeeklyCurriculumPort.save(weeklyCurriculum);
        // TODO: EditWeeklyCurriculumRequest의 isExtra 필드가 primitive boolean이라 null 전달 불가.
        //  "변경하지 않음" 의미를 표현하려면 Request를 Boolean(박싱)으로 변경 필요.
        //  현재는 요청 시 항상 isExtra 값이 업데이트됨.
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