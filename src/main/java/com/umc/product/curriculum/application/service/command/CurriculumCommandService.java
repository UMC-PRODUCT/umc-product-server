package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.dto.CurriculumCommand;
import com.umc.product.curriculum.application.port.in.command.dto.CurriculumCommand.WorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.ReleaseWorkbookUseCase;
import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadCurriculumPort;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveCurriculumPort;
import com.umc.product.curriculum.application.port.out.SaveOriginalWorkbookPort;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CurriculumCommandService implements ReleaseWorkbookUseCase {

    private final LoadCurriculumPort loadCurriculumPort;
    private final SaveCurriculumPort saveCurriculumPort;
    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;
    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final SaveOriginalWorkbookPort saveOriginalWorkbookPort;
    private final GetGisuUseCase getGisuUseCase;

    private static Set<Long> findHavingId(CurriculumCommand command) {
        return command.workbooks().stream()
            .filter(WorkbookCommand::hasId)
            .map(WorkbookCommand::id)
            .collect(Collectors.toSet());
    }

    @Override
    public void release(Long workbookId) {
        OriginalWorkbook workbook = loadOriginalWorkbookPort.findById(workbookId);

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
            throw new CurriculumDomainException(CurriculumErrorCode.WORKBOOK_HAS_SUBMISSIONS);
        }
    }

}
