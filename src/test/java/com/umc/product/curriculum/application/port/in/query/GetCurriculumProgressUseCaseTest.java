//package com.umc.product.curriculum.application.port.in.query;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.BDDMockito.given;
//
//import com.umc.product.challenger.adapter.out.persistence.ChallengerWorkbookJpaRepository;
//import com.umc.product.challenger.application.port.in.query.dto.ChallengerInfo;
//import com.umc.product.challenger.domain.ChallengerWorkbook;
//import com.umc.product.common.domain.enums.ChallengerPart;
//import com.umc.product.curriculum.adapter.out.persistence.CurriculumJpaRepository;
//import com.umc.product.curriculum.adapter.out.persistence.OriginalWorkbookJpaRepository;
//import com.umc.product.curriculum.domain.Curriculum;
//import com.umc.product.curriculum.domain.OriginalWorkbook;
//import com.umc.product.curriculum.domain.enums.MissionType;
//import com.umc.product.curriculum.domain.enums.WorkbookStatus;
//import com.umc.product.global.exception.BusinessException;
//import com.umc.product.organization.application.port.out.command.ManageGisuPort;
//import com.umc.product.organization.domain.Gisu;
//import com.umc.product.support.UseCaseTestSupport;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//class GetCurriculumProgressUseCaseTest extends UseCaseTestSupport {
//
//    @Autowired
//    private GetCurriculumProgressUseCase getCurriculumProgressUseCase;
//
//    @Autowired
//    private CurriculumJpaRepository curriculumJpaRepository;
//
//    @Autowired
//    private OriginalWorkbookJpaRepository originalWorkbookJpaRepository;
//
//    @Autowired
//    private ChallengerWorkbookJpaRepository challengerWorkbookJpaRepository;
//
//    @Autowired
//    private ManageGisuPort manageGisuPort;
//
//    private static final Long CHALLENGER_ID = 1L;
//    private static final Long MEMBER_ID = 1L;
//    private static final String CURRICULUM_TITLE = "9기 Springboot";
//    private static final LocalDate BASE_START_DATE = LocalDate.of(2024, 3, 1);
//
//    @BeforeEach
//    void setUp() {
//        given(getChallengerUseCase.getChallengerPublicInfo(CHALLENGER_ID))
//                .willReturn(ChallengerInfo.builder()
//                        .challengerId(CHALLENGER_ID)
//                        .memberId(MEMBER_ID)
//                        .gisu(1L)
//                        .part(ChallengerPart.SPRINGBOOT)
//                        .build());
//    }
//
//    @Test
//    void 활성_기수의_커리큘럼_진행_상황을_조회한다() {
//        // given
//        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
//        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(activeGisu.getId(), ChallengerPart.SPRINGBOOT));
//
//        OriginalWorkbook workbook1 = createOriginalWorkbook(curriculum, 1, "1주차 - Spring 시작하기", true);
//        OriginalWorkbook workbook2 = createOriginalWorkbook(curriculum, 2, "2주차 - JPA 기초", true);
//        OriginalWorkbook workbook3 = createOriginalWorkbook(curriculum, 3, "3주차 - REST API", false);
//        originalWorkbookJpaRepository.saveAll(List.of(workbook1, workbook2, workbook3));
//
//        // when
//        CurriculumProgressInfo result = getCurriculumProgressUseCase.getMyProgress(CHALLENGER_ID);
//
//        // then
//        assertThat(result.curriculumId()).isEqualTo(curriculum.getId());
//        assertThat(result.curriculumTitle()).isEqualTo(CURRICULUM_TITLE);
//        assertThat(result.part()).isEqualTo(ChallengerPart.SPRINGBOOT.name());
//        assertThat(result.completedCount()).isZero();
//        assertThat(result.totalCount()).isEqualTo(3);
//        assertThat(result.workbooks()).hasSize(3);
//
//        // 1주차: 배포됨, 미제출
//        CurriculumProgressInfo.WorkbookProgressInfo week1 = result.workbooks().get(0);
//        assertThat(week1.weekNo()).isEqualTo(1);
//        assertThat(week1.title()).isEqualTo("1주차 - Spring 시작하기");
//        assertThat(week1.status()).isEqualTo(WorkbookStatus.PENDING);
//        assertThat(week1.isReleased()).isTrue();
//
//        // 3주차: 미배포
//        CurriculumProgressInfo.WorkbookProgressInfo week3 = result.workbooks().get(2);
//        assertThat(week3.weekNo()).isEqualTo(3);
//        assertThat(week3.status()).isNull();
//        assertThat(week3.isReleased()).isFalse();
//    }
//
//    @Test
//    void 챌린저_워크북이_있으면_해당_상태를_반환한다() {
//        // given
//        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
//        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(activeGisu.getId(), ChallengerPart.SPRINGBOOT));
//
//        OriginalWorkbook workbook1 = originalWorkbookJpaRepository.save(
//                createOriginalWorkbook(curriculum, 1, "1주차 - Spring 시작하기", true));
//        OriginalWorkbook workbook2 = originalWorkbookJpaRepository.save(
//                createOriginalWorkbook(curriculum, 2, "2주차 - JPA 기초", true));
//
//        // 챌린저 워크북 생성 (1주차: PASS, 2주차: SUBMITTED)
//        challengerWorkbookJpaRepository.save(createChallengerWorkbook(CHALLENGER_ID, workbook1.getId(), WorkbookStatus.PASS));
//        challengerWorkbookJpaRepository.save(createChallengerWorkbook(CHALLENGER_ID, workbook2.getId(), WorkbookStatus.SUBMITTED));
//
//        // when
//        CurriculumProgressInfo result = getCurriculumProgressUseCase.getMyProgress(CHALLENGER_ID);
//
//        // then
//        assertThat(result.workbooks()).hasSize(2);
//        assertThat(result.completedCount()).isEqualTo(1);
//        assertThat(result.totalCount()).isEqualTo(2);
//
//        CurriculumProgressInfo.WorkbookProgressInfo week1 = result.workbooks().get(0);
//        assertThat(week1.status()).isEqualTo(WorkbookStatus.PASS);
//        assertThat(week1.challengerWorkbookId()).isNotNull();
//
//        CurriculumProgressInfo.WorkbookProgressInfo week2 = result.workbooks().get(1);
//        assertThat(week2.status()).isEqualTo(WorkbookStatus.SUBMITTED);
//        assertThat(week2.challengerWorkbookId()).isNotNull();
//    }
//
//    @Test
//    void 완료_개수는_PASS_FAIL만_집계한다() {
//        // given
//        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
//        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(activeGisu.getId(), ChallengerPart.SPRINGBOOT));
//
//        OriginalWorkbook workbook1 = originalWorkbookJpaRepository.save(
//                createOriginalWorkbook(curriculum, 1, "1주차", true));
//        OriginalWorkbook workbook2 = originalWorkbookJpaRepository.save(
//                createOriginalWorkbook(curriculum, 2, "2주차", true));
//        OriginalWorkbook workbook3 = originalWorkbookJpaRepository.save(
//                createOriginalWorkbook(curriculum, 3, "3주차", true));
//        originalWorkbookJpaRepository.save(createOriginalWorkbook(curriculum, 4, "4주차", false));
//
//        challengerWorkbookJpaRepository.save(createChallengerWorkbook(CHALLENGER_ID, workbook1.getId(), WorkbookStatus.PASS));
//        challengerWorkbookJpaRepository.save(createChallengerWorkbook(CHALLENGER_ID, workbook2.getId(), WorkbookStatus.FAIL));
//        challengerWorkbookJpaRepository.save(createChallengerWorkbook(CHALLENGER_ID, workbook3.getId(), WorkbookStatus.SUBMITTED));
//
//        // when
//        CurriculumProgressInfo result = getCurriculumProgressUseCase.getMyProgress(CHALLENGER_ID);
//
//        // then
//        assertThat(result.completedCount()).isEqualTo(2);
//        assertThat(result.totalCount()).isEqualTo(4);
//    }
//
//    @Test
//    void 비활성_기수의_커리큘럼은_조회되지_않는다() {
//        // given
//        Gisu inactiveGisu = manageGisuPort.save(createInactiveGisu(8L));
//        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(inactiveGisu.getId(), ChallengerPart.SPRINGBOOT));
//        originalWorkbookJpaRepository.save(createOriginalWorkbook(curriculum, 1, "1주차", true));
//
//        // when & then
//        assertThatThrownBy(() -> getCurriculumProgressUseCase.getMyProgress(CHALLENGER_ID))
//                .isInstanceOf(BusinessException.class);
//    }
//
//    @Test
//    void 다른_파트의_커리큘럼은_조회되지_않는다() {
//        // given
//        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
//        // WEB 파트 커리큘럼만 생성 (SPRINGBOOT 아님)
//        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(activeGisu.getId(), ChallengerPart.WEB));
//        originalWorkbookJpaRepository.save(createOriginalWorkbook(curriculum, 1, "1주차", true));
//
//        // when & then
//        assertThatThrownBy(() -> getCurriculumProgressUseCase.getMyProgress(CHALLENGER_ID))
//                .isInstanceOf(BusinessException.class);
//    }
//
//    @Test
//    void 현재_주차에_해당하는_워크북은_isInProgress가_true이다() {
//        // given
//        LocalDate today = LocalDate.now();
//        Gisu activeGisu = manageGisuPort.save(Gisu.builder()
//                .generation(9L)
//                .isActive(true)
//                .startAt(today.minusDays(7).atStartOfDay())  // 1주 전 시작
//                .endAt(today.plusDays(70).atStartOfDay())
//                .build());
//
//        Curriculum curriculum = curriculumJpaRepository.save(
//                Curriculum.builder()
//                        .gisuId(activeGisu.getId())
//                        .part(ChallengerPart.SPRINGBOOT)
//                        .title(CURRICULUM_TITLE)
//                        .build());
//
//        OriginalWorkbook workbook1 = createOriginalWorkbook(
//                curriculum, 1, "1주차", true, today.minusDays(14), today.minusDays(8));
//        OriginalWorkbook workbook2 = createOriginalWorkbook(
//                curriculum, 2, "2주차", true, today.minusDays(3), today.plusDays(3));
//        OriginalWorkbook workbook3 = createOriginalWorkbook(
//                curriculum, 3, "3주차", true, today.plusDays(7), today.plusDays(13));
//        originalWorkbookJpaRepository.saveAll(List.of(workbook1, workbook2, workbook3));
//
//        // when
//        CurriculumProgressInfo result = getCurriculumProgressUseCase.getMyProgress(CHALLENGER_ID);
//
//        // then
//        assertThat(result.workbooks().get(0).isInProgress()).isFalse(); // 1주차
//        assertThat(result.workbooks().get(1).isInProgress()).isTrue();  // 2주차 (현재)
//        assertThat(result.workbooks().get(2).isInProgress()).isFalse(); // 3주차
//    }
//
//    @Test
//    void 배포되지_않은_워크북은_기간이_맞아도_isInProgress가_false이다() {
//        // given
//        LocalDate today = LocalDate.now();
//        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
//        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(activeGisu.getId(), ChallengerPart.SPRINGBOOT));
//
//        OriginalWorkbook workbook = createOriginalWorkbook(
//                curriculum, 1, "1주차", false, today.minusDays(1), today.plusDays(1));
//        originalWorkbookJpaRepository.save(workbook);
//
//        // when
//        CurriculumProgressInfo result = getCurriculumProgressUseCase.getMyProgress(CHALLENGER_ID);
//
//        // then
//        assertThat(result.workbooks().get(0).isInProgress()).isFalse();
//    }
//
//    @Test
//    void 미션타입이_워크북_정보에_포함된다() {
//        // given
//        Gisu activeGisu = manageGisuPort.save(createActiveGisu(9L));
//        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(activeGisu.getId(), ChallengerPart.SPRINGBOOT));
//
//        OriginalWorkbook workbook = OriginalWorkbook.builder()
//                .curriculum(curriculum)
//                .weekNo(1)
//                .title("1주차")
//                .startDate(BASE_START_DATE)
//                .endDate(BASE_START_DATE.plusDays(6))
//                .missionType(MissionType.LINK)
//                .build();
//        workbook.release();
//        originalWorkbookJpaRepository.save(workbook);
//
//        // when
//        CurriculumProgressInfo result = getCurriculumProgressUseCase.getMyProgress(CHALLENGER_ID);
//
//        // then
//        assertThat(result.workbooks().get(0).missionType()).isEqualTo(MissionType.LINK);
//    }
//
//    private Gisu createActiveGisu(Long generation) {
//        return Gisu.builder()
//                .generation(generation)
//                .isActive(true)
//                .startAt(LocalDateTime.of(2024, 3, 1, 0, 0))
//                .endAt(LocalDateTime.of(2024, 8, 31, 23, 59))
//                .build();
//    }
//
//    private Gisu createInactiveGisu(Long generation) {
//        return Gisu.builder()
//                .generation(generation)
//                .isActive(false)
//                .startAt(LocalDateTime.of(2023, 3, 1, 0, 0))
//                .endAt(LocalDateTime.of(2023, 8, 31, 23, 59))
//                .build();
//    }
//
//    private Curriculum createCurriculum(Long gisuId, ChallengerPart part) {
//        return Curriculum.builder()
//                .gisuId(gisuId)
//                .part(part)
//                .title(CURRICULUM_TITLE)
//                .build();
//    }
//
//    private OriginalWorkbook createOriginalWorkbook(Curriculum curriculum, int weekNo, String title, boolean released) {
//        LocalDate startDate = BASE_START_DATE.plusDays((long) (weekNo - 1) * 7);
//        LocalDate endDate = startDate.plusDays(6);
//        return createOriginalWorkbook(curriculum, weekNo, title, released, startDate, endDate);
//    }
//
//    private OriginalWorkbook createOriginalWorkbook(Curriculum curriculum, int weekNo, String title, boolean released,
//                                                    LocalDate startDate, LocalDate endDate) {
//        OriginalWorkbook workbook = OriginalWorkbook.builder()
//                .curriculum(curriculum)
//                .weekNo(weekNo)
//                .title(title)
//                .startDate(startDate)
//                .endDate(endDate)
//                .missionType(MissionType.LINK)
//                .build();
//        if (released) {
//            workbook.release();
//        }
//        return workbook;
//    }
//
//    private ChallengerWorkbook createChallengerWorkbook(Long challengerId, Long originalWorkbookId, WorkbookStatus status) {
//        return ChallengerWorkbook.builder()
//                .challengerId(challengerId)
//                .originalWorkbookId(originalWorkbookId)
//                .scheduleId(1L)
//                .status(status)
//                .build();
//    }
//}
