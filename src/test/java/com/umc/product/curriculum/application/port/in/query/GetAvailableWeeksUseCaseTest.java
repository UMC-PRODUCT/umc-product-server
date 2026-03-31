package com.umc.product.curriculum.application.port.in.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.domain.Curriculum;
import com.umc.product.organization.domain.Gisu;
import com.umc.product.support.UseCaseTestSupport;
import com.umc.product.support.fixture.CurriculumFixture;
import com.umc.product.support.fixture.GisuFixture;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GetOriginalWorkbookUseCaseTest extends UseCaseTestSupport {

    @Autowired
    private GetOriginalWorkbookUseCase getAvailableWeeksUseCase;

    @Autowired
    private GisuFixture gisuFixture;

    @Autowired
    private CurriculumFixture curriculumFixture;

    @Nested
    @DisplayName("배포된 주차 목록 조회")
    class GetAvailableWeeksTest {

        @Test
        @DisplayName("특정 파트의 배포된 주차만 조회된다")
        void 특정_파트의_배포된_주차만_조회된다() {
            // given
            Gisu gisu = gisuFixture.활성_기수(9L);
            Curriculum springCurriculum = curriculumFixture.커리큘럼(gisu.getId(), ChallengerPart.SPRINGBOOT);

            curriculumFixture.배포된_워크북(springCurriculum, 1, "1주차");
            curriculumFixture.배포된_워크북(springCurriculum, 2, "2주차");
            curriculumFixture.워크북(springCurriculum, 3, "3주차"); // 미배포

            // when
            List<Integer> result = getAvailableWeeksUseCase.getAvailableWeeks(ChallengerPart.SPRINGBOOT);

            // then
            assertThat(result).containsExactly(1, 2);
        }

        @Test
        @DisplayName("파트가 null이면 모든 파트의 배포된 주차가 합산되어 조회된다")
        void 파트가_null이면_모든_파트의_배포된_주차가_합산되어_조회된다() {
            // given
            Gisu gisu = gisuFixture.활성_기수(9L);

            // 스프링 커리큘럼: 1~3주차 배포
            Curriculum springCurriculum = curriculumFixture.커리큘럼(gisu.getId(), ChallengerPart.SPRINGBOOT);
            for (int i = 1; i <= 3; i++) {
                curriculumFixture.배포된_워크북(springCurriculum, i, i + "주차");
            }

            // 노드 커리큘럼: 1~5주차 배포
            Curriculum nodeCurriculum = curriculumFixture.커리큘럼(gisu.getId(), ChallengerPart.NODEJS);
            for (int i = 1; i <= 5; i++) {
                curriculumFixture.배포된_워크북(nodeCurriculum, i, i + "주차");
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
            Gisu gisu = gisuFixture.활성_기수(9L);
            Curriculum curriculum = curriculumFixture.커리큘럼(gisu.getId(), ChallengerPart.SPRINGBOOT);

            // 모두 미배포
            curriculumFixture.워크북(curriculum, 1, "1주차");
            curriculumFixture.워크북(curriculum, 2, "2주차");

            // when
            List<Integer> result = getAvailableWeeksUseCase.getAvailableWeeks(ChallengerPart.SPRINGBOOT);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("비활성 기수의 워크북은 조회되지 않는다")
        void 비활성_기수의_워크북은_조회되지_않는다() {
            // given
            Gisu inactiveGisu = gisuFixture.비활성_기수(8L);
            Curriculum curriculum = curriculumFixture.커리큘럼(inactiveGisu.getId(), ChallengerPart.SPRINGBOOT);

            curriculumFixture.배포된_워크북(curriculum, 1, "1주차");

            // when
            List<Integer> result = getAvailableWeeksUseCase.getAvailableWeeks(ChallengerPart.SPRINGBOOT);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("주차 번호가 오름차순으로 정렬된다")
        void 주차_번호가_오름차순으로_정렬된다() {
            // given
            Gisu gisu = gisuFixture.활성_기수(9L);
            Curriculum curriculum = curriculumFixture.커리큘럼(gisu.getId(), ChallengerPart.SPRINGBOOT);

            // 역순으로 생성
            for (int i = 5; i >= 1; i--) {
                curriculumFixture.배포된_워크북(curriculum, i, i + "주차");
            }

            // when
            List<Integer> result = getAvailableWeeksUseCase.getAvailableWeeks(ChallengerPart.SPRINGBOOT);

            // then
            assertThat(result).containsExactly(1, 2, 3, 4, 5);
        }
    }
}
