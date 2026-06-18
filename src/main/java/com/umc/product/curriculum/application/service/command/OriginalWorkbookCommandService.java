package com.umc.product.curriculum.application.service.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umc.product.audit.application.port.in.annotation.Audited;
import com.umc.product.audit.domain.AuditAction;
import com.umc.product.curriculum.application.port.in.command.AutoReleaseWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageOriginalWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.ChangeOriginalWorkbookStatusCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.CreateOriginalWorkbookCommand;
import com.umc.product.curriculum.application.port.in.command.dto.workbook.EditOriginalWorkbookCommand;
import com.umc.product.curriculum.application.port.out.LoadChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.LoadWeeklyCurriculumPort;
import com.umc.product.curriculum.application.port.out.SaveOriginalWorkbookPort;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.WeeklyCurriculum;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import com.umc.product.global.exception.NotImplementedException;
import com.umc.product.global.exception.constant.Domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OriginalWorkbookCommandService implements ManageOriginalWorkbookUseCase, AutoReleaseWorkbookUseCase {

    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final SaveOriginalWorkbookPort saveOriginalWorkbookPort;
    private final LoadWeeklyCurriculumPort loadWeeklyCurriculumPort;
    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;

    @Override
    public List<Long> createBulk(List<CreateOriginalWorkbookCommand> commands) {
        if (commands.isEmpty()) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>(commands.size());
        for (CreateOriginalWorkbookCommand command : commands) {
            ids.add(create(command));
        }
        return ids;
    }

    @Audited(
        domain = Domain.CURRICULUM,
        action = AuditAction.CREATE,
        targetType = "OriginalWorkbook",
        targetId = "#result",
        description = "'원본 워크북이 생성되었습니다.'"
    )
    @Override
    public Long create(CreateOriginalWorkbookCommand command) {
        WeeklyCurriculum weeklyCurriculum = loadWeeklyCurriculumPort.getById(command.weeklyCurriculumId());

        OriginalWorkbook workbook = switch (command.initialStatus()) {
            case DRAFT -> OriginalWorkbook.createAsDraft(
                weeklyCurriculum, command.title(), command.description(),
                command.url(), command.content(), command.type()
            );
            case READY -> OriginalWorkbook.createAsReady(
                weeklyCurriculum, command.title(), command.description(),
                command.url(), command.content(), command.type()
            );
            default -> throw new CurriculumDomainException(CurriculumErrorCode.INVALID_WORKBOOK_STATUS);
        };

        return saveOriginalWorkbookPort.save(workbook).getId();
    }

    @Audited(
        domain = Domain.CURRICULUM,
        action = AuditAction.UPDATE,
        targetType = "OriginalWorkbook",
        targetId = "#command.originalWorkbookId()",
        description = "'원본 워크북이 수정되었습니다.'"
    )
    @Override
    public void edit(EditOriginalWorkbookCommand command) {
        OriginalWorkbook workbook = loadOriginalWorkbookPort.getById(command.originalWorkbookId());
        workbook.edit(command.title(), command.description(), command.url(), command.content());
        saveOriginalWorkbookPort.save(workbook);
    }

    @Audited(
        domain = Domain.CURRICULUM,
        action = AuditAction.DELETE,
        targetType = "OriginalWorkbook",
        targetId = "#originalWorkbookId",
        description = "'원본 워크북이 삭제되었습니다.'"
    )
    @Override
    public void delete(Long originalWorkbookId) {
        OriginalWorkbook workbook = loadOriginalWorkbookPort.getById(originalWorkbookId);
        if (loadChallengerWorkbookPort.existsByOriginalWorkbookId(originalWorkbookId)) {
            throw new CurriculumDomainException(CurriculumErrorCode.WORKBOOK_HAS_SUBMISSIONS);
        }
        saveOriginalWorkbookPort.delete(workbook);
    }

    @Override
    public void changeStatusForRelease(List<ChangeOriginalWorkbookStatusCommand> commands) {
        List<Long> ids = commands.stream()
            .map(ChangeOriginalWorkbookStatusCommand::originalWorkbookId)
            .toList();

        Map<Long, OriginalWorkbook> workbookById = loadOriginalWorkbookPort.batchGetByIds(ids).stream()
            .collect(Collectors.toMap(OriginalWorkbook::getId, w -> w));

        for (ChangeOriginalWorkbookStatusCommand command : commands) {
            workbookById.get(command.originalWorkbookId())
                .changeStatus(command.status(), command.requestedMemberId());
        }

        saveOriginalWorkbookPort.saveAll(new ArrayList<>(workbookById.values()));
    }

    // ===== AutoReleaseWorkbookUseCase =====

    @Audited(
        domain = Domain.CURRICULUM,
        action = AuditAction.PUBLISH,
        targetType = "OriginalWorkbook",
        description = "'배포 예정 워크북 자동 배포가 실행되었습니다.'"
    )
    @Override
    public int releaseAllDue() {
        throw new NotImplementedException();
    }
}
