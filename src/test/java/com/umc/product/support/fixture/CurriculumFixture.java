package com.umc.product.support.fixture;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.out.SaveChallengerWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveCurriculumPort;
import com.umc.product.curriculum.application.port.out.SaveOriginalWorkbookPort;
import com.umc.product.curriculum.domain.ChallengerWorkbook;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class CurriculumFixture {

    private static final Instant DEFAULT_START = Instant.parse("2024-03-01T00:00:00Z");
    private static final Instant DEFAULT_END = Instant.parse("2024-03-07T23:59:59Z");

    private final SaveCurriculumPort saveCurriculumPort;
    private final SaveOriginalWorkbookPort saveOriginalWorkbookPort;
    private final SaveChallengerWorkbookPort saveChallengerWorkbookPort;

    public CurriculumFixture(
        SaveCurriculumPort saveCurriculumPort,
        SaveOriginalWorkbookPort saveOriginalWorkbookPort,
        SaveChallengerWorkbookPort saveChallengerWorkbookPort
    ) {
        this.saveCurriculumPort = saveCurriculumPort;
        this.saveOriginalWorkbookPort = saveOriginalWorkbookPort;
        this.saveChallengerWorkbookPort = saveChallengerWorkbookPort;
    }

    public Curriculum 커리큘럼(Long gisuId, ChallengerPart part) {
        return saveCurriculumPort.save(Curriculum.create(gisuId, part, "9기 " + part.name()));
    }

    public OriginalWorkbook 워크북(Curriculum curriculum, int weekNo, String title) {
        return 워크북(curriculum, weekNo, title, MissionType.LINK);
    }

    public OriginalWorkbook 워크북(Curriculum curriculum, int weekNo, String title, MissionType missionType) {
        return saveOriginalWorkbookPort.save(
            OriginalWorkbook.create(curriculum, weekNo, title, null, null, DEFAULT_START, DEFAULT_END, missionType)
        );
    }

    public OriginalWorkbook 배포된_워크북(Curriculum curriculum, int weekNo, String title) {
        OriginalWorkbook workbook = OriginalWorkbook.create(
            curriculum, weekNo, title, null, null, DEFAULT_START, DEFAULT_END, MissionType.LINK);
        workbook.release();
        return saveOriginalWorkbookPort.save(workbook);
    }

    public ChallengerWorkbook 챌린저워크북(Long challengerId, Long workbookId, WorkbookStatus status) {
        return saveChallengerWorkbookPort.save(
            ChallengerWorkbook.create(challengerId, workbookId,  status, 1L)
        );
    }

    public ChallengerWorkbook 제출된_챌린저워크북(Long challengerId, Long workbookId, String submission) {
        ChallengerWorkbook workbook = ChallengerWorkbook.create(challengerId, workbookId, WorkbookStatus.PENDING, 1L);
        workbook.submit(MissionType.LINK, submission);
        return saveChallengerWorkbookPort.save(workbook);
    }
}
