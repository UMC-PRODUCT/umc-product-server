package com.umc.product.curriculum.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.application.port.out.SaveCurriculumPort;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.support.UseCaseTestSupport;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GetAdminCurriculumUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetAdminCurriculumUseCase getAdminCurriculumUseCase;

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
    void 활성_기수의_커리큘럼을_조회한다() {
        // given
        Curriculum curriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "9기 Springboot");
        OriginalWorkbook workbook1 = OriginalWorkbook.create(
            curriculum, 1, "1주차 - Spring 시작하기", "Spring Boot 기초", "http://workbook1.url",
            Instant.parse("2024-03-01T00:00:00Z"), Instant.parse("2024-03-07T23:59:59Z"), MissionType.LINK
        );
        OriginalWorkbook workbook2 = OriginalWorkbook.create(
            curriculum, 2, "2주차 - JPA 기초", "JPA 입문", "http://workbook2.url",
            Instant.parse("2024-03-08T00:00:00Z"), Instant.parse("2024-03-14T23:59:59Z"), MissionType.MEMO
        );
        curriculum.addWorkbook(workbook1);
        curriculum.addWorkbook(workbook2);
        saveCurriculumPort.save(curriculum);

        // when
        AdminCurriculumInfo result = getAdminCurriculumUseCase.getByActiveGisuAndPart(ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("9기 Springboot");
        assertThat(result.part()).isEqualTo(ChallengerPart.SPRINGBOOT);
        assertThat(result.workbooks()).hasSize(2);
    }

    @Test
    void 워크북은_주차순으로_정렬된다() {
        // given
        Curriculum curriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "커리큘럼");
        OriginalWorkbook workbook3 = OriginalWorkbook.create(
            curriculum, 3, "3주차", null, null,
            Instant.parse("2024-03-15T00:00:00Z"), Instant.parse("2024-03-21T23:59:59Z"), MissionType.LINK
        );
        OriginalWorkbook workbook1 = OriginalWorkbook.create(
            curriculum, 1, "1주차", null, null,
            Instant.parse("2024-03-01T00:00:00Z"), Instant.parse("2024-03-07T23:59:59Z"), MissionType.LINK
        );
        OriginalWorkbook workbook2 = OriginalWorkbook.create(
            curriculum, 2, "2주차", null, null,
            Instant.parse("2024-03-08T00:00:00Z"), Instant.parse("2024-03-14T23:59:59Z"), MissionType.LINK
        );
        // 순서 섞어서 추가
        curriculum.addWorkbook(workbook3);
        curriculum.addWorkbook(workbook1);
        curriculum.addWorkbook(workbook2);
        saveCurriculumPort.save(curriculum);

        // when
        AdminCurriculumInfo result = getAdminCurriculumUseCase.getByActiveGisuAndPart(ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result.workbooks()).hasSize(3);
        assertThat(result.workbooks().get(0).weekNo()).isEqualTo(1);
        assertThat(result.workbooks().get(1).weekNo()).isEqualTo(2);
        assertThat(result.workbooks().get(2).weekNo()).isEqualTo(3);
    }

    @Test
    void 워크북_정보가_올바르게_매핑된다() {
        // given
        Curriculum curriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "커리큘럼");
        OriginalWorkbook workbook = OriginalWorkbook.create(
            curriculum, 1, "1주차 제목", "1주차 설명", "http://workbook.url",
            Instant.parse("2024-03-01T00:00:00Z"), Instant.parse("2024-03-07T23:59:59Z"), MissionType.MEMO
        );
        curriculum.addWorkbook(workbook);
        saveCurriculumPort.save(curriculum);

        // when
        AdminCurriculumInfo result = getAdminCurriculumUseCase.getByActiveGisuAndPart(ChallengerPart.SPRINGBOOT);

        // then
        AdminCurriculumInfo.WorkbookInfo workbookInfo = result.workbooks().get(0);
        assertThat(workbookInfo.id()).isNotNull();
        assertThat(workbookInfo.weekNo()).isEqualTo(1);
        assertThat(workbookInfo.title()).isEqualTo("1주차 제목");
        assertThat(workbookInfo.description()).isEqualTo("1주차 설명");
        assertThat(workbookInfo.workbookUrl()).isEqualTo("http://workbook.url");
        assertThat(workbookInfo.startDate()).isEqualTo(Instant.parse("2024-03-01T00:00:00Z"));
        assertThat(workbookInfo.endDate()).isEqualTo(Instant.parse("2024-03-07T23:59:59Z"));
        assertThat(workbookInfo.missionType()).isEqualTo(MissionType.MEMO);
        assertThat(workbookInfo.isReleased()).isFalse();
        assertThat(workbookInfo.releasedAt()).isNull();
    }

    @Test
    void 배포된_워크북은_배포_정보가_포함된다() {
        // given
        Curriculum curriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "커리큘럼");
        OriginalWorkbook workbook = OriginalWorkbook.create(
            curriculum, 1, "1주차", null, null,
            Instant.parse("2024-03-01T00:00:00Z"), Instant.parse("2024-03-07T23:59:59Z"), MissionType.LINK
        );
        workbook.release();
        curriculum.addWorkbook(workbook);
        saveCurriculumPort.save(curriculum);

        // when
        AdminCurriculumInfo result = getAdminCurriculumUseCase.getByActiveGisuAndPart(ChallengerPart.SPRINGBOOT);

        // then
        AdminCurriculumInfo.WorkbookInfo workbookInfo = result.workbooks().get(0);
        assertThat(workbookInfo.isReleased()).isTrue();
        assertThat(workbookInfo.releasedAt()).isNotNull();
    }

    @Test
    void 커리큘럼이_없으면_null을_반환한다() {
        // given - 커리큘럼 없음

        // when
        AdminCurriculumInfo result = getAdminCurriculumUseCase.getByActiveGisuAndPart(ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result).isNull();
    }

    @Test
    void 다른_파트의_커리큘럼은_조회되지_않는다() {
        // given
        Curriculum webCurriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.WEB, "9기 WEB");
        saveCurriculumPort.save(webCurriculum);

        // when
        AdminCurriculumInfo result = getAdminCurriculumUseCase.getByActiveGisuAndPart(ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result).isNull();
    }

    @Test
    void 비활성_기수의_커리큘럼은_조회되지_않는다() {
        // given
        Gisu inactiveGisu = manageGisuPort.save(createInactiveGisu(8L));
        Curriculum curriculum = Curriculum.create(inactiveGisu.getId(), ChallengerPart.SPRINGBOOT, "8기 Springboot");
        saveCurriculumPort.save(curriculum);

        // when
        AdminCurriculumInfo result = getAdminCurriculumUseCase.getByActiveGisuAndPart(ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result).isNull();
    }

    private Gisu createActiveGisu(Long generation) {
        return Gisu.create(
            generation,
            Instant.parse("2024-03-01T00:00:00Z"),
            Instant.parse("2024-08-31T23:59:59Z"),
            true
        );
    }

    private Gisu createInactiveGisu(Long generation) {
        return Gisu.create(
            generation,
            Instant.parse("2023-03-01T00:00:00Z"),
            Instant.parse("2023-08-31T23:59:59Z"),
            false
        );
    }
}
