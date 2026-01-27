package com.umc.product.curriculum.application.port.in.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.challenger.domain.Challenger;
import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.out.persistence.ChallengerWorkbookJpaRepository;
import com.umc.product.curriculum.adapter.out.persistence.CurriculumJpaRepository;
import com.umc.product.curriculum.adapter.out.persistence.OriginalWorkbookJpaRepository;
import com.umc.product.curriculum.domain.ChallengerWorkbook;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.curriculum.domain.enums.WorkbookStatus;
import com.umc.product.global.exception.BusinessException;
import com.umc.product.member.domain.Member;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.application.port.out.command.ManageSchoolPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.organization.domain.School;
import com.umc.product.support.TestChallengerRepository;
import com.umc.product.support.TestMemberRepository;
import com.umc.product.support.UseCaseTestSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ManageAdminChallengerWorkbookUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private ManageAdminChallengerWorkbookUseCase manageAdminChallengerWorkbookUseCase;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Autowired
    private ManageSchoolPort manageSchoolPort;

    @Autowired
    private TestMemberRepository memberRepository;

    @Autowired
    private TestChallengerRepository challengerRepository;

    @Autowired
    private CurriculumJpaRepository curriculumJpaRepository;

    @Autowired
    private OriginalWorkbookJpaRepository originalWorkbookJpaRepository;

    @Autowired
    private ChallengerWorkbookJpaRepository challengerWorkbookJpaRepository;

    @Nested
    class 워크북_검토 {

        @Test
        void 워크북을_통과_처리한다() {
            // given
            ChallengerWorkbook workbook = createWorkbookWithStatus(WorkbookStatus.SUBMITTED);
            String feedback = "잘 작성하셨습니다!";

            ReviewWorkbookCommand command = new ReviewWorkbookCommand(
                    workbook.getId(),
                    WorkbookStatus.PASS,
                    feedback
            );

            // when
            manageAdminChallengerWorkbookUseCase.review(command);

            // then
            ChallengerWorkbook result = challengerWorkbookJpaRepository.findById(workbook.getId()).orElseThrow();
            assertThat(result.getStatus()).isEqualTo(WorkbookStatus.PASS);
            assertThat(result.getFeedback()).isEqualTo(feedback);
        }

        @Test
        void 워크북을_반려_처리한다() {
            // given
            ChallengerWorkbook workbook = createWorkbookWithStatus(WorkbookStatus.SUBMITTED);
            String feedback = "내용이 부족합니다. 보완해주세요.";

            ReviewWorkbookCommand command = new ReviewWorkbookCommand(
                    workbook.getId(),
                    WorkbookStatus.FAIL,
                    feedback
            );

            // when
            manageAdminChallengerWorkbookUseCase.review(command);

            // then
            ChallengerWorkbook result = challengerWorkbookJpaRepository.findById(workbook.getId()).orElseThrow();
            assertThat(result.getStatus()).isEqualTo(WorkbookStatus.FAIL);
            assertThat(result.getFeedback()).isEqualTo(feedback);
        }

        @Test
        void 피드백_없이_통과_처리할_수_있다() {
            // given
            ChallengerWorkbook workbook = createWorkbookWithStatus(WorkbookStatus.SUBMITTED);

            ReviewWorkbookCommand command = new ReviewWorkbookCommand(
                    workbook.getId(),
                    WorkbookStatus.PASS,
                    null
            );

            // when
            manageAdminChallengerWorkbookUseCase.review(command);

            // then
            ChallengerWorkbook result = challengerWorkbookJpaRepository.findById(workbook.getId()).orElseThrow();
            assertThat(result.getStatus()).isEqualTo(WorkbookStatus.PASS);
            assertThat(result.getFeedback()).isNull();
        }

        @Test
        void 이미_검토된_워크북은_다시_검토할_수_없다() {
            // given
            ChallengerWorkbook workbook = createWorkbookWithStatus(WorkbookStatus.SUBMITTED);

            manageAdminChallengerWorkbookUseCase.review(new ReviewWorkbookCommand(
                    workbook.getId(),
                    WorkbookStatus.PASS,
                    null
            ));

            // when & then
            ReviewWorkbookCommand command = new ReviewWorkbookCommand(
                    workbook.getId(),
                    WorkbookStatus.FAIL,
                    "다시 검토"
            );

            assertThatThrownBy(() -> manageAdminChallengerWorkbookUseCase.review(command))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        void 존재하지_않는_워크북을_검토하면_예외가_발생한다() {
            // given
            ReviewWorkbookCommand command = new ReviewWorkbookCommand(
                    999L,
                    WorkbookStatus.PASS,
                    "피드백"
            );

            // when & then
            assertThatThrownBy(() -> manageAdminChallengerWorkbookUseCase.review(command))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    class 베스트_워크북_선정 {

        @Test
        void 통과된_워크북을_베스트로_선정한다() {
            // given
            ChallengerWorkbook workbook = createWorkbookWithStatus(WorkbookStatus.PASS);
            String bestReason = "꼼꼼한 분석과 창의적인 접근이 돋보입니다.";

            SelectBestWorkbookCommand command = new SelectBestWorkbookCommand(
                    workbook.getId(),
                    bestReason
            );

            // when
            manageAdminChallengerWorkbookUseCase.selectBest(command);

            // then
            ChallengerWorkbook result = challengerWorkbookJpaRepository.findById(workbook.getId()).orElseThrow();
            assertThat(result.getStatus()).isEqualTo(WorkbookStatus.BEST);
            assertThat(result.getBestReason()).isEqualTo(bestReason);
        }

        @Test
        void 제출_상태의_워크북을_바로_베스트로_선정한다() {
            // given
            ChallengerWorkbook workbook = createWorkbookWithStatus(WorkbookStatus.SUBMITTED);
            String bestReason = "바로 베스트 선정!";

            SelectBestWorkbookCommand command = new SelectBestWorkbookCommand(
                    workbook.getId(),
                    bestReason
            );

            // when
            manageAdminChallengerWorkbookUseCase.selectBest(command);

            // then
            ChallengerWorkbook result = challengerWorkbookJpaRepository.findById(workbook.getId()).orElseThrow();
            assertThat(result.getStatus()).isEqualTo(WorkbookStatus.BEST);
            assertThat(result.getBestReason()).isEqualTo(bestReason);
        }

        @Test
        void 베스트_선정_이유_없이_베스트로_선정한다() {
            // given
            ChallengerWorkbook workbook = createWorkbookWithStatus(WorkbookStatus.SUBMITTED);

            SelectBestWorkbookCommand command = new SelectBestWorkbookCommand(
                    workbook.getId(),
                    null
            );

            // when
            manageAdminChallengerWorkbookUseCase.selectBest(command);

            // then
            ChallengerWorkbook result = challengerWorkbookJpaRepository.findById(workbook.getId()).orElseThrow();
            assertThat(result.getStatus()).isEqualTo(WorkbookStatus.BEST);
            assertThat(result.getBestReason()).isNull();
        }

        @Test
        void 반려된_워크북은_베스트로_선정할_수_없다() {
            // given
            ChallengerWorkbook workbook = createWorkbookWithStatus(WorkbookStatus.FAIL);

            SelectBestWorkbookCommand command = new SelectBestWorkbookCommand(
                    workbook.getId(),
                    "베스트 선정 이유"
            );

            // when & then
            assertThatThrownBy(() -> manageAdminChallengerWorkbookUseCase.selectBest(command))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        void 존재하지_않는_워크북을_베스트로_선정하면_예외가_발생한다() {
            // given
            SelectBestWorkbookCommand command = new SelectBestWorkbookCommand(
                    999L,
                    "베스트 선정 이유"
            );

            // when & then
            assertThatThrownBy(() -> manageAdminChallengerWorkbookUseCase.selectBest(command))
                    .isInstanceOf(BusinessException.class);
        }
    }

    private ChallengerWorkbook createWorkbookWithStatus(WorkbookStatus status) {
        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));
        Member member = memberRepository.save(createMember("홍길동", school.getId()));
        Challenger challenger = challengerRepository.save(
                new Challenger(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));
        OriginalWorkbook originalWorkbook = originalWorkbookJpaRepository.save(createWorkbook(curriculum, 1, "1주차 워크북"));

        return challengerWorkbookJpaRepository.save(
                ChallengerWorkbook.builder()
                        .challengerId(challenger.getId())
                        .originalWorkbookId(originalWorkbook.getId())
                        .scheduleId(1L)
                        .status(status)
                        .build()
        );
    }

    private Gisu createActiveGisu(Long generation) {
        return Gisu.builder()
                .generation(generation)
                .isActive(true)
                .startAt(LocalDateTime.of(2024, 3, 1, 0, 0))
                .endAt(LocalDateTime.of(2024, 8, 31, 23, 59))
                .build();
    }

    private Member createMember(String nickname, Long schoolId) {
        return Member.builder()
                .name(nickname)
                .nickname(nickname)
                .email(nickname + "@test.com")
                .schoolId(schoolId)
                .build();
    }

    private Curriculum createCurriculum(Long gisuId, ChallengerPart part) {
        return Curriculum.create(gisuId, part, "9기 " + part.name());
    }

    private OriginalWorkbook createWorkbook(Curriculum curriculum, int weekNo, String title) {
        return OriginalWorkbook.create(
                curriculum,
                weekNo,
                title,
                null,
                null,
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 3, 7),
                MissionType.LINK
        );
    }
}
