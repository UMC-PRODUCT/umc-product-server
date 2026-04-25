package com.umc.product.curriculum.adapter.out.persistence;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumInfo.WorkbookInfo;
import com.umc.product.curriculum.application.port.in.query.dto.CurriculumWeekInfo;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveOriginalWorkbookPort;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import com.umc.product.global.exception.NotImplementedException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OriginalWorkbookPersistenceAdapter implements LoadOriginalWorkbookPort, SaveOriginalWorkbookPort {

    private final OriginalWorkbookJpaRepository originalWorkbookJpaRepository;
    private final CurriculumQueryRepository curriculumQueryRepository;

    @Override
    public OriginalWorkbook findById(Long id) {
        return originalWorkbookJpaRepository.findById(id)
            .orElseThrow(() -> new CurriculumDomainException(CurriculumErrorCode.WORKBOOK_NOT_FOUND));
    }

    @Override
    public List<OriginalWorkbook> findByCurriculumId(Long curriculumId) {
        throw new NotImplementedException();
//        return originalWorkbookJpaRepository.findByCurriculumId(curriculumId);
    }

    @Override
    public List<OriginalWorkbook> findByCurriculumIdOrderByWeekNo(Long curriculumId) {
        throw new NotImplementedException();
//        return originalWorkbookJpaRepository.findByCurriculumIdOrderByWeekNoAsc(curriculumId);
    }

    @Override
    public List<OriginalWorkbook> findReleasedByWeeklyCurriculumId(Long weeklyCurriculumId) {
        return originalWorkbookJpaRepository.findByWeeklyCurriculumIdAndOriginalWorkbookStatus(
            weeklyCurriculumId, OriginalWorkbookStatus.RELEASED);
    }

    @Override
    public List<Integer> findDistinctWeekNoByGisuId(Long gisuId) {
        throw new NotImplementedException();
//        return originalWorkbookJpaRepository.findDistinctWeekNoByGisuId(gisuId);
    }

    @Override
    public List<CurriculumWeekInfo> findWeekInfoByActiveGisuAndPart(ChallengerPart part) {
        return curriculumQueryRepository.findWeekInfoByActiveGisuAndPart(part);
    }

    @Override
    public List<Integer> findReleasedWeekNos(ChallengerPart part) {
        return curriculumQueryRepository.findReleasedWeekNos(part);
    }

    @Override
    public List<WorkbookInfo> findWorkbookInfos(Long curriculumId, Integer weekNo) {
        return curriculumQueryRepository.fetchWorkbooks(curriculumId, weekNo);
    }

    @Override
    public OriginalWorkbook save(OriginalWorkbook workbook) {
        return originalWorkbookJpaRepository.save(workbook);
    }

    @Override
    public List<OriginalWorkbook> findUnreleasedWithStartDateBefore(Instant now) {
        return curriculumQueryRepository.findUnreleasedWorkbookIdsWithStartDateBefore(now);
    }

    @Override
    public List<OriginalWorkbook> findByCurriculumIdIn(List<Long> curriculumIds) {
        if (curriculumIds == null || curriculumIds.isEmpty()) {
            return List.of();
        }

        throw new NotImplementedException();
//        return originalWorkbookJpaRepository.findByCurriculumIdIn(curriculumIds);
    }
}
