package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.CurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.CurriculumCommand.WorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.ManageCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.command.ReleaseWorkbookUseCase;
import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadCurriculumPort;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveCurriculumPort;
import com.umc.product.curriculum.application.port.out.SaveOriginalWorkbookPort;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.global.exception.constant.Domain;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CurriculumCommandService implements ManageCurriculumUseCase, ReleaseWorkbookUseCase {

    private final LoadCurriculumPort loadCurriculumPort;
    private final SaveCurriculumPort saveCurriculumPort;
    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;
    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final SaveOriginalWorkbookPort saveOriginalWorkbookPort;
    private final GetGisuUseCase getGisuUseCase;

    @Override
    public void manage(CurriculumCommand command) {
        Curriculum curriculum = loadCurriculumPort.findByActiveGisuAndPart(command.part())
                .orElseGet(() -> Curriculum.create(getGisuUseCase.getActiveGisuId(), command.part(), command.title()));

        curriculum.updateTitle(command.title());

        Map<Long, OriginalWorkbook> existingWorkbookMap = curriculum.getOriginalWorkbooks().stream()
                .collect(Collectors.toMap(OriginalWorkbook::getId, Function.identity()));

        Set<Long> requestWorkbookIds = findHavingId(command);

        List<OriginalWorkbook> workbooksToDelete = findWorkbooksToDelete(existingWorkbookMap, requestWorkbookIds);

        // 챌린저에게 배포되었다면 커리큘럼에서 original_workbook 삭제 불가능
        validateNoSubmissions(workbooksToDelete);

        workbooksToDelete.forEach(curriculum::removeOriginalWorkbook);

        for (WorkbookCommand workbookCommand : command.workbooks()) {
            if (workbookCommand.hasId()) {
                // 요청받은 workbookCommand.id가 실제로 존재하는 workbook인지 체크
                OriginalWorkbook existingWorkbook = existingWorkbookMap.get(workbookCommand.id());
                if (existingWorkbook == null) {
                    throw new BusinessException(Domain.CURRICULUM, CurriculumErrorCode.WORKBOOK_NOT_IN_CURRICULUM);
                }
                existingWorkbook.update(
                        workbookCommand.title(),
                        workbookCommand.description(),
                        workbookCommand.workbookUrl(),
                        workbookCommand.weekNo(),
                        workbookCommand.startDate(),
                        workbookCommand.endDate(),
                        workbookCommand.missionType()
                );
            } else {
                OriginalWorkbook newWorkbook = OriginalWorkbook.create(
                        curriculum,
                        workbookCommand.weekNo(),
                        workbookCommand.title(),
                        workbookCommand.description(),
                        workbookCommand.workbookUrl(),
                        workbookCommand.resolveStartDate(),
                        workbookCommand.resolveEndDate(),
                        workbookCommand.resolveMissionType()
                );
                curriculum.addWorkbook(newWorkbook);
            }
        }

        saveCurriculumPort.save(curriculum);
    }

    private static Set<Long> findHavingId(CurriculumCommand command) {
        return command.workbooks().stream()
                .filter(WorkbookCommand::hasId)
                .map(WorkbookCommand::id)
                .collect(Collectors.toSet());
    }

    @Override
    public void release(Long workbookId) {
        OriginalWorkbook workbook = loadOriginalWorkbookPort.findById(workbookId)
                .orElseThrow(() -> new BusinessException(Domain.CURRICULUM, CurriculumErrorCode.WORKBOOK_NOT_FOUND));

        workbook.release();
        saveOriginalWorkbookPort.save(workbook);
    }


    private List<OriginalWorkbook> findWorkbooksToDelete(Map<Long, OriginalWorkbook> existingWorkbookMap,
                                                         Set<Long> requestWorkbookIds) {
        return existingWorkbookMap.entrySet().stream()
                .filter(entry -> !requestWorkbookIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
    }

    private void validateNoSubmissions(List<OriginalWorkbook> workbooksToDelete) {
        if (workbooksToDelete.isEmpty()) {
            return;
        }

        List<Long> workbookIds = workbooksToDelete.stream()
                .map(OriginalWorkbook::getId)
                .toList();

        // 챌린저에게 배포되었다면 커리큘럼에서 original_workbook 삭제 불가능
        List<Long> workbookIdsWithSubmissions = loadChallengerWorkbookPort
                .findOriginalWorkbookIdsWithSubmissions(workbookIds);

        if (!workbookIdsWithSubmissions.isEmpty()) {
            throw new BusinessException(Domain.CURRICULUM, CurriculumErrorCode.WORKBOOK_HAS_SUBMISSIONS);
        }
    }
    
}
