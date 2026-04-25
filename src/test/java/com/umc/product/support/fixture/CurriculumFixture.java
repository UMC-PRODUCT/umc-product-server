package com.umc.product.support.fixture;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.out.SaveCurriculumPort;
import com.umc.product.curriculum.application.port.out.SaveOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveWeeklyCurriculumPort;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.WeeklyCurriculum;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookType;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class CurriculumFixture {

    private static final Instant DEFAULT_START = Instant.parse("2024-03-01T00:00:00Z");
    private static final Instant DEFAULT_END = Instant.parse("2024-03-07T23:59:59Z");

    private final SaveCurriculumPort saveCurriculumPort;
    private final SaveWeeklyCurriculumPort saveWeeklyCurriculumPort;
    private final SaveOriginalWorkbookPort saveOriginalWorkbookPort;

    public CurriculumFixture(
        SaveCurriculumPort saveCurriculumPort,
        SaveWeeklyCurriculumPort saveWeeklyCurriculumPort,
        SaveOriginalWorkbookPort saveOriginalWorkbookPort
    ) {
        this.saveCurriculumPort = saveCurriculumPort;
        this.saveWeeklyCurriculumPort = saveWeeklyCurriculumPort;
        this.saveOriginalWorkbookPort = saveOriginalWorkbookPort;
    }

    public Curriculum 커리큘럼(Long gisuId, ChallengerPart part) {
        return saveCurriculumPort.save(Curriculum.create(gisuId, part, "9기 " + part.name()));
    }

    public WeeklyCurriculum 주차별_커리큘럼(Curriculum curriculum, Long weekNo, String title) {
        return saveWeeklyCurriculumPort.save(
            WeeklyCurriculum.create(curriculum, weekNo, false, title, DEFAULT_START, DEFAULT_END)
        );
    }

    public OriginalWorkbook 워크북(WeeklyCurriculum weeklyCurriculum, String title) {
        return saveOriginalWorkbookPort.save(
            OriginalWorkbook.createAsDraft(weeklyCurriculum, title, null, null, null, OriginalWorkbookType.MAIN)
        );
    }

    public OriginalWorkbook 배포된_워크북(WeeklyCurriculum weeklyCurriculum, String title) {
        // TODO: ChallengerWorkbook 배포 시 챌린저 워크북 생성 로직도 함께 구현 필요
        OriginalWorkbook workbook = OriginalWorkbook.createAsReady(
            weeklyCurriculum, title, null, null, null, OriginalWorkbookType.MAIN
        );
        workbook.changeStatus(OriginalWorkbookStatus.RELEASED, null);
        return saveOriginalWorkbookPort.save(workbook);
    }

    // TODO: ChallengerWorkbook.create() 미구현 — 챌린저 워크북 관련 fixture는 구현 후 추가 필요
}