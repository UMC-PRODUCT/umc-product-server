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
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GetCurriculumWeeksUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetCurriculumProgressUseCase getCurriculumProgressUseCase;

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
    void 파트별_커리큘럼_주차_목록을_조회한다() {
        // given
        Curriculum curriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "9기 Springboot");
        curriculum.addWorkbook(createWorkbook(curriculum, 1, "1주차 - Spring 시작하기"));
        curriculum.addWorkbook(createWorkbook(curriculum, 2, "2주차 - JPA 기초"));
        curriculum.addWorkbook(createWorkbook(curriculum, 3, "3주차 - REST API"));
        saveCurriculumPort.save(curriculum);

        // when
        List<CurriculumWeekInfo> result = getCurriculumProgressUseCase.getWeeksByPart(ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).weekNo()).isEqualTo(1);
        assertThat(result.get(0).title()).isEqualTo("1주차 - Spring 시작하기");
        assertThat(result.get(1).weekNo()).isEqualTo(2);
        assertThat(result.get(1).title()).isEqualTo("2주차 - JPA 기초");
        assertThat(result.get(2).weekNo()).isEqualTo(3);
        assertThat(result.get(2).title()).isEqualTo("3주차 - REST API");
    }

    @Test
    void 주차_번호_순서대로_정렬되어_반환된다() {
        // given
        Curriculum curriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "9기 Springboot");
        curriculum.addWorkbook(createWorkbook(curriculum, 3, "3주차"));
        curriculum.addWorkbook(createWorkbook(curriculum, 1, "1주차"));
        curriculum.addWorkbook(createWorkbook(curriculum, 2, "2주차"));
        saveCurriculumPort.save(curriculum);

        // when
        List<CurriculumWeekInfo> result = getCurriculumProgressUseCase.getWeeksByPart(ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).weekNo()).isEqualTo(1);
        assertThat(result.get(1).weekNo()).isEqualTo(2);
        assertThat(result.get(2).weekNo()).isEqualTo(3);
    }

    @Test
    void 다른_파트의_커리큘럼은_조회되지_않는다() {
        // given
        Curriculum springbootCurriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "9기 Springboot");
        springbootCurriculum.addWorkbook(createWorkbook(springbootCurriculum, 1, "Spring 1주차"));
        saveCurriculumPort.save(springbootCurriculum);

        Curriculum webCurriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.WEB, "9기 Web");
        webCurriculum.addWorkbook(createWorkbook(webCurriculum, 1, "Web 1주차"));
        saveCurriculumPort.save(webCurriculum);

        // when
        List<CurriculumWeekInfo> result = getCurriculumProgressUseCase.getWeeksByPart(ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Spring 1주차");
    }

    @Test
    void 비활성_기수의_커리큘럼은_조회되지_않는다() {
        // given
        Gisu inactiveGisu = manageGisuPort.save(createInactiveGisu(8L));
        Curriculum curriculum = Curriculum.create(inactiveGisu.getId(), ChallengerPart.SPRINGBOOT, "8기 Springboot");
        curriculum.addWorkbook(createWorkbook(curriculum, 1, "1주차"));
        saveCurriculumPort.save(curriculum);

        // when
        List<CurriculumWeekInfo> result = getCurriculumProgressUseCase.getWeeksByPart(ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result).isEmpty();
    }


    @Test
    void 워크북이_없는_커리큘럼은_빈_리스트를_반환한다() {
        // given
        Curriculum curriculum = Curriculum.create(activeGisu.getId(), ChallengerPart.SPRINGBOOT, "9기 Springboot");
        saveCurriculumPort.save(curriculum);

        // when
        List<CurriculumWeekInfo> result = getCurriculumProgressUseCase.getWeeksByPart(ChallengerPart.SPRINGBOOT);

        // then
        assertThat(result).isEmpty();
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

    private OriginalWorkbook createWorkbook(Curriculum curriculum, int weekNo, String title) {
        Instant startBase = Instant.parse("2024-03-01T00:00:00Z");
        Instant endBase = Instant.parse("2024-03-07T00:00:00Z");
        return OriginalWorkbook.create(
                curriculum,
                weekNo,
                title,
                null,
                null,
                startBase.plus((long) (weekNo - 1) * 7, ChronoUnit.DAYS),
                endBase.plus((long) (weekNo - 1) * 7, ChronoUnit.DAYS),
                MissionType.LINK
        );
    }
}
