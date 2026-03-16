package com.umc.product.curriculum.application.service.query;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumProgressUseCase;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumInfo;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumProgressInfo;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumProgressInfo.WorkbookProgressInfo;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumProjection;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumWeekInfo;
import com.umc.product.curriculum.application.port.in.query.dto.WorkbookProgressProjection;
import com.umc.product.curriculum.application.port.out.LoadCurriculumPort;
import com.umc.product.curriculum.application.port.out.LoadCurriculumProgressPort;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurriculumQueryService implements GetCurriculumProgressUseCase, GetCurriculumUseCase {

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final LoadCurriculumProgressPort loadCurriculumProgressPort;
    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final LoadCurriculumPort loadCurriculumPort;

    @Override
    public CurriculumInfo getByGisuAndPart(Long gisuId, ChallengerPart part, Integer weekNo) {
        CurriculumProjection projection = loadCurriculumPort.getByGisuIdAndPart(gisuId, part);
        List<CurriculumInfo.WorkbookInfo> workbooks = loadOriginalWorkbookPort.findWorkbookInfos(projection.id(), weekNo);
        return new CurriculumInfo(projection.id(), projection.part(), projection.title(), workbooks);
    }

    @Override
    @Deprecated
    public CurriculumProgressInfo getMyProgress(Long memberId) {
        Long activeGisuId = getGisuUseCase.getActiveGisuId();
        return getMyProgressByGisu(memberId, activeGisuId);
    }

    @Override
    public CurriculumProgressInfo getMyProgressByGisu(Long memberId, Long gisuId) {
        ChallengerInfo challengerInfo = getChallengerUseCase.getByMemberIdAndGisuId(memberId, gisuId);
        CurriculumProjection projection = loadCurriculumPort.getByGisuIdAndPart(gisuId, challengerInfo.part());

        List<WorkbookProgressProjection> workbookProgressProjections = loadCurriculumProgressPort.findWorkbookProgressProjections(
            projection.id(), challengerInfo.challengerId()
        );

        Instant now = Instant.now();
        List<WorkbookProgressInfo> workbooks = workbookProgressProjections.stream()
            .map(workbookProjection -> toWorkbookProgressInfo(workbookProjection, now))
            .toList();

        int completedCount = (int) workbooks.stream()
            .filter(w -> w.status() == WorkbookStatus.PASS || w.status() == WorkbookStatus.FAIL)
            .count();

        return new CurriculumProgressInfo(
            projection.id(),
            projection.title(),
            projection.part().name(),
            completedCount,
            workbooks.size(),
            workbooks
        );
    }

    @Override
    public List<CurriculumWeekInfo> getWeeksByPart(ChallengerPart part) {
        return loadOriginalWorkbookPort.findWeekInfoByActiveGisuAndPart(part);
    }

    private WorkbookProgressInfo toWorkbookProgressInfo(WorkbookProgressProjection row, Instant now) {
        boolean isReleased = row.releasedAt() != null;
        boolean isInDateRange = row.startDate() != null && row.endDate() != null
            && !now.isBefore(row.startDate()) && !now.isAfter(row.endDate());
        WorkbookStatus status = row.challengerWorkbookId() == null ? null : row.challengerWorkbookStatus();

        return new WorkbookProgressInfo(
            row.originalWorkbookId(),
            row.challengerWorkbookId(),
            row.weekNo(),
            row.title(),
            row.description(),
            row.missionType(),
            status,
            isReleased,
            isReleased && isInDateRange
        );
    }
}
