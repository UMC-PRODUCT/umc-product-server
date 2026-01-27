package com.umc.product.curriculum.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.out.LoadOriginalWorkbookPort;
import com.umc.product.curriculum.application.port.out.SaveCurriculumPort;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.support.UseCaseTestSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ReleaseWorkbookUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private ReleaseWorkbookUseCase releaseWorkbookUseCase;

    @Autowired
    private SaveCurriculumPort saveCurriculumPort;

    @Autowired
    private LoadOriginalWorkbookPort loadOriginalWorkbookPort;

    @Autowired
    private ManageGisuPort manageGisuPort;

    private Gisu activeGisu;

    @BeforeEach
    void setUp() {
        activeGisu = manageGisuPort.save(createActiveGisu(9L));
    }

    @Test
    void 워크북을_배포한다() {
        // given
        Curriculum curriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "커리큘럼");
        OriginalWorkbook workbook = OriginalWorkbook.create(
                curriculum, 1, "1주차 - Spring 시작하기", "설명", "http://workbook.url",
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 7), MissionType.LINK
        );
        curriculum.addWorkbook(workbook);
        Curriculum savedCurriculum = saveCurriculumPort.save(curriculum);
        Long workbookId = savedCurriculum.getOriginalWorkbooks().get(0).getId();

        // when
        releaseWorkbookUseCase.release(workbookId);

        // then
        OriginalWorkbook releasedWorkbook = loadOriginalWorkbookPort.findById(workbookId);
        assertThat(releasedWorkbook.isReleased()).isTrue();
        assertThat(releasedWorkbook.getReleasedAt()).isNotNull();
    }

    @Test
    void 존재하지_않는_워크북을_배포하면_예외가_발생한다() {
        // given
        Long nonExistentWorkbookId = 999L;

        // when & then
        assertThatThrownBy(() -> releaseWorkbookUseCase.release(nonExistentWorkbookId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 여러_워크북을_개별적으로_배포할_수_있다() {
        // given
        Curriculum curriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "커리큘럼");
        OriginalWorkbook workbook1 = OriginalWorkbook.create(
                curriculum, 1, "1주차", null, null,
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 7), MissionType.LINK
        );
        OriginalWorkbook workbook2 = OriginalWorkbook.create(
                curriculum, 2, "2주차", null, null,
                LocalDate.of(2024, 3, 8), LocalDate.of(2024, 3, 14), MissionType.LINK
        );
        curriculum.addWorkbook(workbook1);
        curriculum.addWorkbook(workbook2);
        Curriculum savedCurriculum = saveCurriculumPort.save(curriculum);
        Long workbook1Id = savedCurriculum.getOriginalWorkbooks().get(0).getId();
        Long workbook2Id = savedCurriculum.getOriginalWorkbooks().get(1).getId();

        // when
        releaseWorkbookUseCase.release(workbook1Id);

        // then
        OriginalWorkbook released1 = loadOriginalWorkbookPort.findById(workbook1Id);
        OriginalWorkbook notReleased2 = loadOriginalWorkbookPort.findById(workbook2Id);

        assertThat(released1.isReleased()).isTrue();
        assertThat(notReleased2.isReleased()).isFalse();
    }

    private Gisu createActiveGisu(Long generation) {
        return Gisu.builder()
                .generation(generation)
                .isActive(true)
                .startAt(LocalDateTime.of(2024, 3, 1, 0, 0))
                .endAt(LocalDateTime.of(2024, 8, 31, 23, 59))
                .build();
    }
}
