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
import java.time.Instant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ManageWorkbookUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private ManageWorkbookUseCase manageWorkbookUseCase;

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
    class 워크북_제출 {

        @Test
        void 워크북을_제출한다() {
            // given
            ChallengerWorkbook workbook = createWorkbookWithStatus(WorkbookStatus.PENDING);
            String submission = "https://github.com/user/repo";

            SubmitWorkbookCommand command = new SubmitWorkbookCommand(
                    workbook.getId(),
                    submission
            );

            // when
            manageWorkbookUseCase.submit(command);

            // then
            ChallengerWorkbook result = challengerWorkbookJpaRepository.findById(workbook.getId()).orElseThrow();
            assertThat(result.getStatus()).isEqualTo(WorkbookStatus.SUBMITTED);
            assertThat(result.getSubmission()).isEqualTo(submission);
        }

        @Test
        void 이미_제출한_워크북은_다시_제출할_수_없다() {
            // given
            ChallengerWorkbook workbook = createWorkbookWithStatus(WorkbookStatus.SUBMITTED);

            SubmitWorkbookCommand command = new SubmitWorkbookCommand(
                    workbook.getId(),
                    "https://github.com/user/repo"
            );

            // when & then
            assertThatThrownBy(() -> manageWorkbookUseCase.submit(command))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        void PLAIN_타입_워크북은_제출_내용_없이_제출할_수_있다() {
            // given
            ChallengerWorkbook workbook = createWorkbookWithStatus(WorkbookStatus.PENDING, MissionType.PLAIN);

            SubmitWorkbookCommand command = new SubmitWorkbookCommand(
                    workbook.getId(),
                    null
            );

            // when
            manageWorkbookUseCase.submit(command);

            // then
            ChallengerWorkbook result = challengerWorkbookJpaRepository.findById(workbook.getId()).orElseThrow();
            assertThat(result.getStatus()).isEqualTo(WorkbookStatus.SUBMITTED);
            assertThat(result.getSubmission()).isNull();
        }

        @Test
        void LINK_타입_워크북은_제출_내용이_필수다() {
            // given
            ChallengerWorkbook workbook = createWorkbookWithStatus(WorkbookStatus.PENDING, MissionType.LINK);

            SubmitWorkbookCommand command = new SubmitWorkbookCommand(
                    workbook.getId(),
                    null
            );

            // when & then
            assertThatThrownBy(() -> manageWorkbookUseCase.submit(command))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        void 존재하지_않는_워크북을_제출하면_예외가_발생한다() {
            // given
            SubmitWorkbookCommand command = new SubmitWorkbookCommand(
                    999L,
                    "https://github.com/user/repo"
            );

            // when & then
            assertThatThrownBy(() -> manageWorkbookUseCase.submit(command))
                    .isInstanceOf(BusinessException.class);
        }
    }

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
            manageWorkbookUseCase.review(command);

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
            manageWorkbookUseCase.review(command);

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
            manageWorkbookUseCase.review(command);

            // then
            ChallengerWorkbook result = challengerWorkbookJpaRepository.findById(workbook.getId()).orElseThrow();
            assertThat(result.getStatus()).isEqualTo(WorkbookStatus.PASS);
            assertThat(result.getFeedback()).isNull();
        }

        @Test
        void 이미_검토된_워크북은_다시_검토할_수_없다() {
            // given
            ChallengerWorkbook workbook = createWorkbookWithStatus(WorkbookStatus.SUBMITTED);

            manageWorkbookUseCase.review(new ReviewWorkbookCommand(
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

            assertThatThrownBy(() -> manageWorkbookUseCase.review(command))
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
            assertThatThrownBy(() -> manageWorkbookUseCase.review(command))
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
            manageWorkbookUseCase.selectBest(command);

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
            manageWorkbookUseCase.selectBest(command);

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
            manageWorkbookUseCase.selectBest(command);

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
            assertThatThrownBy(() -> manageWorkbookUseCase.selectBest(command))
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
            assertThatThrownBy(() -> manageWorkbookUseCase.selectBest(command))
                    .isInstanceOf(BusinessException.class);
        }
    }

    private ChallengerWorkbook createWorkbookWithStatus(WorkbookStatus status) {
        return createWorkbookWithStatus(status, MissionType.LINK);
    }

    private ChallengerWorkbook createWorkbookWithStatus(WorkbookStatus status, MissionType missionType) {
        Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
        School school = manageSchoolPort.save(School.create("서울대학교", "비고"));
        Member member = memberRepository.save(createMember("홍길동", school.getId()));
        Challenger challenger = challengerRepository.save(
                new Challenger(member.getId(), ChallengerPart.SPRINGBOOT, gisu.getId()));
        Curriculum curriculum = curriculumJpaRepository.save(createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));
        OriginalWorkbook originalWorkbook = originalWorkbookJpaRepository.save(
                createWorkbook(curriculum, 1, "1주차 워크북", missionType));

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
        return Gisu.create(
                generation,
                Instant.parse("2024-03-01T00:00:00Z"),
                Instant.parse("2024-08-31T23:59:59Z"),
                true
        );
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
        return createWorkbook(curriculum, weekNo, title, MissionType.LINK);
    }

    private OriginalWorkbook createWorkbook(Curriculum curriculum, int weekNo, String title, MissionType missionType) {
        return OriginalWorkbook.create(
                curriculum,
                weekNo,
                title,
                null,
                null,
                Instant.parse("2024-03-01T00:00:00Z"),
                Instant.parse("2024-03-07T23:59:59Z"),
                missionType
        );
    }
}
