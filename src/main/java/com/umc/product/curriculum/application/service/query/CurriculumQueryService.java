package com.umc.product.curriculum.application.service.query;

import com.umc.product.challenger.application.port.in.query.GetChallengerUseCase;
import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.GetCurriculumUseCase;
import com.umc.product.curriculum.application.port.in.query.dto.*;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumProgressInfo.WorkbookProgressInfo;
import com.umc.product.curriculum.application.port.out.*;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.OriginalWorkbookMission;
import com.umc.product.curriculum.domain.WeeklyCurriculum;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import com.umc.product.organization.application.port.in.query.GetGisuUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurriculumQueryService implements GetCurriculumUseCase {

    private final GetChallengerUseCase getChallengerUseCase;
    private final GetGisuUseCase getGisuUseCase;
    private final LoadCurriculumProgressPort loadCurriculumProgressPort;
    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final LoadCurriculumPort loadCurriculumPort;
    private final LoadWeeklyCurriculumPort loadWeeklyCurriculumPort;
    private final LoadOriginalWorkbookMissionPort loadOriginalWorkbookMissionPort;
    private final LoadChallengerWorkbookPort loadChallengerWorkbookPort;

    @Override
    public CurriculumInfo getByGisuAndPart(Long gisuId, ChallengerPart part, Integer weekNo) {
        CurriculumProjection projection = loadCurriculumPort.getByGisuIdAndPart(gisuId, part);
        List<CurriculumInfo.WorkbookInfo> workbooks = loadOriginalWorkbookPort.findWorkbookInfos(projection.id(), weekNo);
        return new CurriculumInfo(projection.id(), projection.part(), projection.title(), workbooks);
    }

    @Override
    public CurriculumOverviewInfo getCurriculumOverview(Long gisuId, ChallengerPart part, Long weekNo) {
        CurriculumProjection projection = loadCurriculumPort.getByGisuIdAndPart(gisuId, part);
        List<WeeklyCurriculum> weeklyCurriculums = loadWeeklyCurriculumPort.findByCurriculumId(projection.id(), weekNo);

        List<CurriculumOverviewInfo.WeeklyItem> weeks = weeklyCurriculums.stream()
            .map(wc -> new CurriculumOverviewInfo.WeeklyItem(
                wc.getId(),
                wc.getWeekNo(),
                wc.getTitle(),
                wc.isExtra(),
                wc.getStartsAt(),
                wc.getEndsAt()
            ))
            .toList();

        return new CurriculumOverviewInfo(projection.id(), projection.title(), weeks);
    }

    @Override
    public MyCurriculumInfo getMyProgressV2(Long memberId, Long gisuId) {
        ChallengerInfo challengerInfo = getChallengerUseCase.getByMemberIdAndGisuId(memberId, gisuId);
        CurriculumProjection curriculumProjection = loadCurriculumPort.getByGisuIdAndPart(gisuId, challengerInfo.part());

        List<WeeklyCurriculum> weeklyCurriculums = loadWeeklyCurriculumPort.findByCurriculumId(
            curriculumProjection.id(), null);

        List<MyCurriculumInfo.MyWeeklyCurriculumInfo> weeks = weeklyCurriculums.stream()
            .map(wc -> buildWeeklyItem(wc, memberId))
            .toList();

        return new MyCurriculumInfo(curriculumProjection.id(), curriculumProjection.title(), weeks);
    }

    private MyCurriculumInfo.MyWeeklyCurriculumInfo buildWeeklyItem(WeeklyCurriculum wc, Long memberId) {
        List<OriginalWorkbook> releasedWorkbooks = loadOriginalWorkbookPort.findReleasedByWeeklyCurriculumId(
            wc.getId());

        List<MyCurriculumInfo.MyOriginalWorkbookInfo> workbookItems = releasedWorkbooks.stream()
            .map(wb -> buildOriginalWorkbookItem(wb, memberId))
            .toList();

        return new MyCurriculumInfo.MyWeeklyCurriculumInfo(
            wc.getId(), wc.getWeekNo(), wc.getTitle(), wc.isExtra(),
            wc.getStartsAt(), wc.getEndsAt(), workbookItems
        );
    }

    private MyCurriculumInfo.MyOriginalWorkbookInfo buildOriginalWorkbookItem(OriginalWorkbook wb, Long memberId) {
        List<OriginalWorkbookMission> missions = loadOriginalWorkbookMissionPort.findByOriginalWorkbookId(wb.getId());

        List<MyCurriculumInfo.MyOriginalWorkbookMissionInfo> myOriginalWorkbookMissionInfos = missions.stream()
            .map(m -> new MyCurriculumInfo.MyOriginalWorkbookMissionInfo(
                m.getId(), m.getTitle(), m.getDescription(), m.getMissionType(), m.isNecessary()
                // TODO: MissionSubmission 조회 추가 필요
                //  - LoadMissionSubmissionPort 구현 후 서비스에서 조회 로직 추가
            ))
            .toList();

        Optional<Long> challengerWorkbookId = loadChallengerWorkbookPort
            .findByMemberIdAndOriginalWorkbookId(memberId, wb.getId())
            .map(cw -> cw.getId());

        return new MyCurriculumInfo.MyOriginalWorkbookInfo(
            wb.getId(), wb.getTitle(), wb.getDescription(),
            wb.getUrl(), wb.getType(), myOriginalWorkbookMissionInfos, challengerWorkbookId
        );
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
        CurriculumProjection curriculumProjection = loadCurriculumPort.getByGisuIdAndPart(gisuId, challengerInfo.part());

        List<WorkbookProgressProjection> workbookProgressProjections = loadCurriculumProgressPort.findWorkbookProgressProjections(
            curriculumProjection.id(), challengerInfo.challengerId()
        );

        Instant now = Instant.now();
        List<WorkbookProgressInfo> workbooks = workbookProgressProjections.stream()
            .map(workbookProjection -> toWorkbookProgressInfo(workbookProjection, now))
            .toList();

        int completedCount = (int) workbooks.stream()
            .filter(w -> w.status() == WorkbookStatus.PASS || w.status() == WorkbookStatus.FAIL)
            .count();

        return new CurriculumProgressInfo(
            curriculumProjection.id(),
            curriculumProjection.title(),
            curriculumProjection.part().name(),
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
