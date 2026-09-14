package com.umc.product.curriculum.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.common.domain.enums.ChallengerPart;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookStatus;
import com.umc.product.curriculum.domain.enums.OriginalWorkbookType;
import com.umc.product.curriculum.domain.exception.CurriculumDomainException;
import com.umc.product.curriculum.domain.exception.CurriculumErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class OriginalWorkbookStatusTransitionTest {

    // ===== canTransitionTo 행렬 검증 =====

    @Nested
    @DisplayName("DRAFT 상태 전환 규칙")
    class FromDraft {

        @Test
        void DRAFT에서_READY로_전환_가능() {
            assertThat(OriginalWorkbookStatus.DRAFT.canTransitionTo(OriginalWorkbookStatus.READY)).isTrue();
        }

        @Test
        void DRAFT에서_RELEASED로_직접_전환_불가() {
            assertThat(OriginalWorkbookStatus.DRAFT.canTransitionTo(OriginalWorkbookStatus.RELEASED)).isFalse();
        }

        @Test
        void DRAFT에서_DRAFT로_전환_불가_자기자신() {
            assertThat(OriginalWorkbookStatus.DRAFT.canTransitionTo(OriginalWorkbookStatus.DRAFT)).isFalse();
        }
    }

    @Nested
    @DisplayName("READY 상태 전환 규칙")
    class FromReady {

        @Test
        void READY에서_RELEASED로_전환_가능() {
            assertThat(OriginalWorkbookStatus.READY.canTransitionTo(OriginalWorkbookStatus.RELEASED)).isTrue();
        }

        @Test
        void READY에서_DRAFT로_전환_가능_롤백() {
            assertThat(OriginalWorkbookStatus.READY.canTransitionTo(OriginalWorkbookStatus.DRAFT)).isTrue();
        }

        @Test
        void READY에서_READY로_전환_불가_자기자신() {
            assertThat(OriginalWorkbookStatus.READY.canTransitionTo(OriginalWorkbookStatus.READY)).isFalse();
        }
    }

    @Nested
    @DisplayName("RELEASED 상태 전환 규칙")
    class FromReleased {

        @ParameterizedTest
        @EnumSource(OriginalWorkbookStatus.class)
        void RELEASED_상태에서_어떤_상태로도_전환_불가(OriginalWorkbookStatus target) {
            assertThat(OriginalWorkbookStatus.RELEASED.canTransitionTo(target)).isFalse();
        }
    }

    // ===== OriginalWorkbook.changeStatus() 도메인 메서드 =====

    @Nested
    @DisplayName("OriginalWorkbook 상태 변경 도메인 메서드")
    class ChangeStatus {

        private OriginalWorkbook draftWorkbook;
        private OriginalWorkbook readyWorkbook;
        private OriginalWorkbook releasedWorkbook;

        @BeforeEach
        void setUp() {
            WeeklyCurriculum weeklyCurriculum = WeeklyCurriculum.create(
                Curriculum.create(9L, ChallengerPart.SPRINGBOOT, "9기 스프링부트"),
                1L, false, "1주차",
                java.time.Instant.parse("2024-03-01T00:00:00Z"),
                java.time.Instant.parse("2024-03-07T23:59:59Z")
            );

            draftWorkbook = OriginalWorkbook.createAsDraft(
                weeklyCurriculum, "DRAFT 워크북", null, null, null, OriginalWorkbookType.MAIN);
            readyWorkbook = OriginalWorkbook.createAsReady(
                weeklyCurriculum, "READY 워크북", null, null, null, OriginalWorkbookType.MAIN);
            releasedWorkbook = OriginalWorkbook.createAsReady(
                weeklyCurriculum, "RELEASED 워크북", null, null, null, OriginalWorkbookType.MAIN);
            releasedWorkbook.changeStatus(OriginalWorkbookStatus.RELEASED, 1L);
        }

        @Test
        void DRAFT에서_READY로_전환_성공() {
            draftWorkbook.changeStatus(OriginalWorkbookStatus.READY, 1L);

            assertThat(draftWorkbook.getOriginalWorkbookStatus()).isEqualTo(OriginalWorkbookStatus.READY);
        }

        @Test
        void READY에서_RELEASED로_전환_성공_타임스탬프_기록() {
            Long operatorId = 42L;
            readyWorkbook.changeStatus(OriginalWorkbookStatus.RELEASED, operatorId);

            assertThat(readyWorkbook.getOriginalWorkbookStatus()).isEqualTo(OriginalWorkbookStatus.RELEASED);
            assertThat(readyWorkbook.getReleasedAt()).isNotNull();
            assertThat(readyWorkbook.getReleasedMemberId()).isEqualTo(operatorId);
        }

        @Test
        void READY에서_DRAFT로_롤백_성공() {
            readyWorkbook.changeStatus(OriginalWorkbookStatus.DRAFT, 1L);

            assertThat(readyWorkbook.getOriginalWorkbookStatus()).isEqualTo(OriginalWorkbookStatus.DRAFT);
        }

        @Test
        void DRAFT에서_RELEASED로_직접_전환_시_예외() {
            assertThatThrownBy(() -> draftWorkbook.changeStatus(OriginalWorkbookStatus.RELEASED, 1L))
                .isInstanceOf(CurriculumDomainException.class)
                .extracting("baseCode")
                .isEqualTo(CurriculumErrorCode.INVALID_WORKBOOK_STATUS_TRANSITION);
        }

        @Test
        void RELEASED에서_DRAFT로_전환_시_예외() {
            assertThatThrownBy(() -> releasedWorkbook.changeStatus(OriginalWorkbookStatus.DRAFT, 1L))
                .isInstanceOf(CurriculumDomainException.class)
                .extracting("baseCode")
                .isEqualTo(CurriculumErrorCode.INVALID_WORKBOOK_STATUS_TRANSITION);
        }

        @Test
        void RELEASED에서_READY로_전환_시_예외() {
            assertThatThrownBy(() -> releasedWorkbook.changeStatus(OriginalWorkbookStatus.READY, 1L))
                .isInstanceOf(CurriculumDomainException.class)
                .extracting("baseCode")
                .isEqualTo(CurriculumErrorCode.INVALID_WORKBOOK_STATUS_TRANSITION);
        }

        @Test
        void RELEASED로_전환_시_releasedMemberId가_null이어도_기록된다() {
            // 스케줄러 자동 배포 시 requestedMemberId가 null일 수 있음
            readyWorkbook.changeStatus(OriginalWorkbookStatus.RELEASED, null);

            assertThat(readyWorkbook.getReleasedAt()).isNotNull();
            assertThat(readyWorkbook.getReleasedMemberId()).isNull();
        }
    }

    // ===== 생성 팩토리 =====

    @Nested
    @DisplayName("OriginalWorkbook 생성 팩토리")
    class Factory {

        private WeeklyCurriculum weeklyCurriculum;

        @BeforeEach
        void setUp() {
            weeklyCurriculum = WeeklyCurriculum.create(
                Curriculum.create(9L, ChallengerPart.SPRINGBOOT, "9기 스프링부트"),
                1L, false, "1주차",
                java.time.Instant.parse("2024-03-01T00:00:00Z"),
                java.time.Instant.parse("2024-03-07T23:59:59Z")
            );
        }

        @Test
        void createAsDraft는_DRAFT_상태로_생성된다() {
            OriginalWorkbook workbook = OriginalWorkbook.createAsDraft(
                weeklyCurriculum, "제목", null, null, null, OriginalWorkbookType.MAIN);

            assertThat(workbook.getOriginalWorkbookStatus()).isEqualTo(OriginalWorkbookStatus.DRAFT);
        }

        @Test
        void createAsReady는_READY_상태로_생성된다() {
            OriginalWorkbook workbook = OriginalWorkbook.createAsReady(
                weeklyCurriculum, "제목", null, null, null, OriginalWorkbookType.MAIN);

            assertThat(workbook.getOriginalWorkbookStatus()).isEqualTo(OriginalWorkbookStatus.READY);
        }

        @Test
        void DRAFT_생성_시_releasedAt은_null이다() {
            OriginalWorkbook workbook = OriginalWorkbook.createAsDraft(
                weeklyCurriculum, "제목", null, null, null, OriginalWorkbookType.MAIN);

            assertThat(workbook.getReleasedAt()).isNull();
            assertThat(workbook.getReleasedMemberId()).isNull();
        }
    }
}