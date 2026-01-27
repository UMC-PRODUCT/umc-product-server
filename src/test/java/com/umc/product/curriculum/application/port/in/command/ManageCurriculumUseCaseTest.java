package com.umc.product.curriculum.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.in.command.CurriculumCommand.WorkbookCommand;
import com.umc.product.curriculum.application.port.out.LoadCurriculumPort;
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
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ManageCurriculumUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private ManageCurriculumUseCase manageCurriculumUseCase;

    @Autowired
    private LoadCurriculumPort loadCurriculumPort;

    @Autowired
    private SaveCurriculumPort saveCurriculumPort;

    @Autowired
    private ManageGisuPort manageGisuPort;

    private Gisu activeGisu;

    @BeforeEach
    void setUp() {
        activeGisu = manageGisuPort.save(createActiveGisu(9L));
    }

    @Test
    void 커리큘럼이_없으면_새로_생성한다() {
        // given
        CurriculumCommand command = new CurriculumCommand(
                ChallengerPart.SPRINGBOOT,
                "9기 Springboot 커리큘럼",
                List.of(
                        createWorkbookCommand(null, 1, "1주차 - Spring 시작하기"),
                        createWorkbookCommand(null, 2, "2주차 - JPA 기초")
                )
        );

        // when
        manageCurriculumUseCase.manage(command);

        // then
        Curriculum savedCurriculum = loadCurriculumPort.findByActiveGisuAndPart(ChallengerPart.SPRINGBOOT)
                .orElseThrow();
        assertThat(savedCurriculum.getTitle()).isEqualTo("9기 Springboot 커리큘럼");
        assertThat(savedCurriculum.getPart()).isEqualTo(ChallengerPart.SPRINGBOOT);
        assertThat(savedCurriculum.getOriginalWorkbooks()).hasSize(2);
    }

    @Test
    void 기존_커리큘럼의_제목을_수정한다() {
        // given
        Curriculum curriculum = saveCurriculumPort.save(
                Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "기존 제목")
        );

        CurriculumCommand command = new CurriculumCommand(
                ChallengerPart.SPRINGBOOT,
                "수정된 제목",
                List.of()
        );

        // when
        manageCurriculumUseCase.manage(command);

        // then
        Curriculum updatedCurriculum = loadCurriculumPort.findById(curriculum.getId()).orElseThrow();
        assertThat(updatedCurriculum.getTitle()).isEqualTo("수정된 제목");
    }

    @Test
    void 기존_워크북을_수정한다() {
        // given
        Curriculum curriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "커리큘럼");
        OriginalWorkbook workbook = OriginalWorkbook.create(
                curriculum, 1, "기존 제목", "기존 설명", "http://old.url",
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 7), MissionType.LINK
        );
        curriculum.addWorkbook(workbook);
        Curriculum savedCurriculum = saveCurriculumPort.save(curriculum);
        Long workbookId = savedCurriculum.getOriginalWorkbooks().get(0).getId();

        CurriculumCommand command = new CurriculumCommand(
                ChallengerPart.SPRINGBOOT,
                "커리큘럼",
                List.of(new WorkbookCommand(
                        workbookId, 1, "수정된 제목", "수정된 설명", "http://new.url",
                        LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 7), MissionType.MEMO
                ))
        );

        // when
        manageCurriculumUseCase.manage(command);

        // then
        Curriculum updatedCurriculum = loadCurriculumPort.findById(savedCurriculum.getId()).orElseThrow();
        OriginalWorkbook updatedWorkbook = updatedCurriculum.getOriginalWorkbooks().get(0);
        assertThat(updatedWorkbook.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedWorkbook.getDescription()).isEqualTo("수정된 설명");
        assertThat(updatedWorkbook.getWorkbookUrl()).isEqualTo("http://new.url");
        assertThat(updatedWorkbook.getMissionType()).isEqualTo(MissionType.MEMO);
    }

    @Test
    void 새_워크북을_추가한다() {
        // given
        Curriculum curriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "커리큘럼");
        OriginalWorkbook existingWorkbook = OriginalWorkbook.create(
                curriculum, 1, "1주차", null, null,
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 7), MissionType.LINK
        );
        curriculum.addWorkbook(existingWorkbook);
        Curriculum savedCurriculum = saveCurriculumPort.save(curriculum);
        Long existingWorkbookId = savedCurriculum.getOriginalWorkbooks().get(0).getId();

        CurriculumCommand command = new CurriculumCommand(
                ChallengerPart.SPRINGBOOT,
                "커리큘럼",
                List.of(
                        createWorkbookCommand(existingWorkbookId, 1, "1주차"),
                        createWorkbookCommand(null, 2, "2주차 - 새로 추가")
                )
        );

        // when
        manageCurriculumUseCase.manage(command);

        // then
        Curriculum updatedCurriculum = loadCurriculumPort.findById(savedCurriculum.getId()).orElseThrow();
        assertThat(updatedCurriculum.getOriginalWorkbooks()).hasSize(2);
    }

    @Test
    void 요청에_없는_워크북은_삭제된다() {
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

        // workbook2를 요청에서 제외 → 삭제됨
        CurriculumCommand command = new CurriculumCommand(
                ChallengerPart.SPRINGBOOT,
                "커리큘럼",
                List.of(createWorkbookCommand(workbook1Id, 1, "1주차"))
        );

        // when
        manageCurriculumUseCase.manage(command);

        // then
        Curriculum updatedCurriculum = loadCurriculumPort.findById(savedCurriculum.getId()).orElseThrow();
        assertThat(updatedCurriculum.getOriginalWorkbooks()).hasSize(1);
        assertThat(updatedCurriculum.getOriginalWorkbooks().get(0).getTitle()).isEqualTo("1주차");
    }

    @Test
    void 존재하지_않는_워크북_ID로_수정하면_예외가_발생한다() {
        // given
        Curriculum curriculum = saveCurriculumPort.save(
                Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "커리큘럼")
        );

        CurriculumCommand command = new CurriculumCommand(
                ChallengerPart.SPRINGBOOT,
                "커리큘럼",
                List.of(createWorkbookCommand(999L, 1, "1주차"))
        );

        // when & then
        assertThatThrownBy(() -> manageCurriculumUseCase.manage(command))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 워크북_수정시_null인_필드는_기존_값을_유지한다() {
        // given
        Curriculum curriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "커리큘럼");
        OriginalWorkbook workbook = OriginalWorkbook.create(
                curriculum, 1, "기존 제목", "기존 설명", "http://old.url",
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 7), MissionType.LINK
        );
        curriculum.addWorkbook(workbook);
        Curriculum savedCurriculum = saveCurriculumPort.save(curriculum);
        Long workbookId = savedCurriculum.getOriginalWorkbooks().get(0).getId();

        // startDate, endDate, missionType을 null로 보냄
        CurriculumCommand command = new CurriculumCommand(
                ChallengerPart.SPRINGBOOT,
                "커리큘럼",
                List.of(new WorkbookCommand(
                        workbookId, 1, "수정된 제목", "수정된 설명", "http://new.url",
                        null, null, null
                ))
        );

        // when
        manageCurriculumUseCase.manage(command);

        // then
        Curriculum updatedCurriculum = loadCurriculumPort.findById(savedCurriculum.getId()).orElseThrow();
        OriginalWorkbook updatedWorkbook = updatedCurriculum.getOriginalWorkbooks().get(0);
        assertThat(updatedWorkbook.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedWorkbook.getStartDate()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(updatedWorkbook.getEndDate()).isEqualTo(LocalDate.of(2024, 3, 7));
        assertThat(updatedWorkbook.getMissionType()).isEqualTo(MissionType.LINK);
    }

    private Gisu createActiveGisu(Long generation) {
        return Gisu.builder()
                .generation(generation)
                .isActive(true)
                .startAt(LocalDateTime.of(2024, 3, 1, 0, 0))
                .endAt(LocalDateTime.of(2024, 8, 31, 23, 59))
                .build();
    }

    private WorkbookCommand createWorkbookCommand(Long id, Integer weekNo, String title) {
        return new WorkbookCommand(
                id, weekNo, title, null, null,
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 7), MissionType.LINK
        );
    }
}
