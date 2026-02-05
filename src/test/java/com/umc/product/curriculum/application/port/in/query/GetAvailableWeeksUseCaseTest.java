package com.umc.product.curriculum.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.adapter.out.persistence.CurriculumJpaRepository;
import com.umc.product.curriculum.adapter.out.persistence.OriginalWorkbookJpaRepository;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.curriculum.domain.OriginalWorkbook;
import com.umc.product.curriculum.domain.enums.MissionType;
import com.umc.product.organization.application.port.out.command.ManageGisuPort;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.support.UseCaseTestSupport;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GetAvailableWeeksUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetAvailableWeeksUseCase getAvailableWeeksUseCase;

    @Autowired
    private ManageGisuPort manageGisuPort;

    @Autowired
    private CurriculumJpaRepository curriculumJpaRepository;

    @Autowired
    private OriginalWorkbookJpaRepository originalWorkbookJpaRepository;

    @Nested
    @DisplayName("배포된 주차 목록 조회")
    class GetAvailableWeeksTest {

        @Test
        @DisplayName("특정 파트의 배포된 주차만 조회된다")
        void 특정_파트의_배포된_주차만_조회된다() {
            // given
            Gisu gisu = manageGisuPort.save(createActiveGisu(9L));

            Curriculum springCurriculum = curriculumJpaRepository.save(
                    createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));

            OriginalWorkbook released1 = originalWorkbookJpaRepository.save(
                    createWorkbook(springCurriculum, 1, "1주차"));
            released1.release();
            originalWorkbookJpaRepository.save(released1);

            OriginalWorkbook released2 = originalWorkbookJpaRepository.save(
                    createWorkbook(springCurriculum, 2, "2주차"));
            released2.release();
            originalWorkbookJpaRepository.save(released2);

            // 3주차는 미배포
            originalWorkbookJpaRepository.save(
                    createWorkbook(springCurriculum, 3, "3주차"));

            // when
            List<Integer> result = getAvailableWeeksUseCase.getAvailableWeeks(ChallengerPart.SPRINGBOOT);

            // then
            assertThat(result).containsExactly(1, 2);
        }

        @Test
        @DisplayName("파트가 null이면 모든 파트의 배포된 주차가 합산되어 조회된다")
        void 파트가_null이면_모든_파트의_배포된_주차가_합산되어_조회된다() {
            // given
            Gisu gisu = manageGisuPort.save(createActiveGisu(9L));

            // 스프링 커리큘럼: 1~3주차 배포
            Curriculum springCurriculum = curriculumJpaRepository.save(
                    createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));
            for (int i = 1; i <= 3; i++) {
                OriginalWorkbook wb = originalWorkbookJpaRepository.save(
                        createWorkbook(springCurriculum, i, i + "주차"));
                wb.release();
                originalWorkbookJpaRepository.save(wb);
            }

            // 노드 커리큘럼: 1~5주차 배포
            Curriculum nodeCurriculum = curriculumJpaRepository.save(
                    createCurriculum(gisu.getId(), ChallengerPart.NODEJS));
            for (int i = 1; i <= 5; i++) {
                OriginalWorkbook wb = originalWorkbookJpaRepository.save(
                        createWorkbook(nodeCurriculum, i, i + "주차"));
                wb.release();
                originalWorkbookJpaRepository.save(wb);
            }

            // when - 회장 (part=null: 모든 파트)
            List<Integer> result = getAvailableWeeksUseCase.getAvailableWeeks(null);

            // then - 1~5주차 (중복 제거)
            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }

        @Test
        @DisplayName("미배포 워크북은 조회되지 않는다")
        void 미배포_워크북은_조회되지_않는다() {
            // given
            Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
            Curriculum curriculum = curriculumJpaRepository.save(
                    createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));

            // 모두 미배포
            originalWorkbookJpaRepository.save(createWorkbook(curriculum, 1, "1주차"));
            originalWorkbookJpaRepository.save(createWorkbook(curriculum, 2, "2주차"));

            // when
            List<Integer> result = getAvailableWeeksUseCase.getAvailableWeeks(ChallengerPart.SPRINGBOOT);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("비활성 기수의 워크북은 조회되지 않는다")
        void 비활성_기수의_워크북은_조회되지_않는다() {
            // given
            Gisu inactiveGisu = manageGisuPort.save(Gisu.create(
                    8L,
                    Instant.parse("2023-03-01T00:00:00Z"),
                    Instant.parse("2023-08-31T23:59:59Z"),
                    false
            ));
            Curriculum curriculum = curriculumJpaRepository.save(
                    createCurriculum(inactiveGisu.getId(), ChallengerPart.SPRINGBOOT));

            OriginalWorkbook wb = originalWorkbookJpaRepository.save(
                    createWorkbook(curriculum, 1, "1주차"));
            wb.release();
            originalWorkbookJpaRepository.save(wb);

            // when
            List<Integer> result = getAvailableWeeksUseCase.getAvailableWeeks(ChallengerPart.SPRINGBOOT);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("주차 번호가 오름차순으로 정렬된다")
        void 주차_번호가_오름차순으로_정렬된다() {
            // given
            Gisu gisu = manageGisuPort.save(createActiveGisu(9L));
            Curriculum curriculum = curriculumJpaRepository.save(
                    createCurriculum(gisu.getId(), ChallengerPart.SPRINGBOOT));

            // 역순으로 생성
            for (int i = 5; i >= 1; i--) {
                OriginalWorkbook wb = originalWorkbookJpaRepository.save(
                        createWorkbook(curriculum, i, i + "주차"));
                wb.release();
                originalWorkbookJpaRepository.save(wb);
            }

            // when
            List<Integer> result = getAvailableWeeksUseCase.getAvailableWeeks(ChallengerPart.SPRINGBOOT);

            // then
            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }
    }

    // ========== Fixture 메서드 ==========

    private Gisu createActiveGisu(Long generation) {
        return Gisu.create(
                generation,
                Instant.parse("2024-03-01T00:00:00Z"),
                Instant.parse("2024-08-31T23:59:59Z"),
                true
        );
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
                Instant.parse("2024-03-01T00:00:00Z"),
                Instant.parse("2024-03-07T23:59:59Z"),
                MissionType.LINK
        );
    }
}
