package com.umc.product.curriculum.application.service.command;

import com.umc.product.curriculum.application.port.in.command.AutoReleaseWorkbookUseCase;
import com.umc.product.curriculum.application.port.in.command.ManageOriginalWorkbookUseCase;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveOriginalWorkbookPort;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OriginalWorkbookCommandService implements ManageOriginalWorkbookUseCase, AutoReleaseWorkbookUseCase {

    private final LoadOriginalWorkbookPort loadOriginalWorkbookPort;
    private final SaveOriginalWorkbookPort saveOriginalWorkbookPort;

    @Override
    public void release(Long workbookId) {
        OriginalWorkbook workbook = loadOriginalWorkbookPort.findById(workbookId);

        workbook.release();
        saveOriginalWorkbookPort.save(workbook);
    }

    /**
     * 배포 조건을 만족하는 모든 워크북을 일괄 배포합니다.
     * <p>
     * 연쇄 배포 방지: 조회를 먼저 완료한 후 배포를 수행합니다.
     */
    @Override
    public int releaseAllDue() {
        Instant now = Instant.now();

        // 1. 미배포 & 시작일 지난 워크북 전부 조회 (연쇄 배포 방지)
        List<OriginalWorkbook> candidates = loadOriginalWorkbookPort.findUnreleasedWithStartDateBefore(now);

        if (candidates.isEmpty()) {
            log.info("[WorkbookAutoRelease] 배포 대상 워크북 없음");
            return 0;
        }

        // 2. 커리큘럼별 배포된 주차 Set 조회 (N+1 방지)
        List<Long> curriculumIds = candidates.stream()
            .map(w -> w.getCurriculum().getId())
            .distinct()
            .toList();

        Map<Long, Set<Integer>> releasedWeeksByCurriculum = loadOriginalWorkbookPort
            .findByCurriculumIdIn(curriculumIds)
            .stream()
            .filter(OriginalWorkbook::isReleased)
            .collect(Collectors.groupingBy(
                w -> w.getCurriculum().getId(),
                Collectors.mapping(OriginalWorkbook::getWeekNo, Collectors.toSet())
            ));

        // 3. 직전 주차 배포 여부 확인하여 필터링
        List<OriginalWorkbook> toRelease = candidates.stream()
            .filter(candidate -> {
                if (candidate.getWeekNo() == 1) {
                    return true;
                }
                Set<Integer> releasedWeeks = releasedWeeksByCurriculum
                    .getOrDefault(candidate.getCurriculum().getId(), Set.of());
                return releasedWeeks.contains(candidate.getWeekNo() - 1);
            })
            .toList();

        if (toRelease.isEmpty()) {
            log.info("[WorkbookAutoRelease] 직전 주차 미배포로 배포 대상 없음 (후보: {}건)", candidates.size());
            return 0;
        }

        // 4. 한 번에 배포
        for (OriginalWorkbook workbook : toRelease) {
            workbook.release();
            log.info("[WorkbookAutoRelease] 워크북 배포 완료: id={}, weekNo={}, title={}",
                workbook.getId(), workbook.getWeekNo(), workbook.getTitle());
        }

        log.info("[WorkbookAutoRelease] 총 {}건 배포 완료", toRelease.size());
        return toRelease.size();
    }
}
