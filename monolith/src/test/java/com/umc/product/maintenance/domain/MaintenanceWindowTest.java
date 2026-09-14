package com.umc.product.maintenance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.umc.product.maintenance.exception.MaintenanceDomainException;
import com.umc.product.maintenance.exception.MaintenanceErrorCode;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MaintenanceWindowTest {

    private static final Instant NOW = Instant.parse("2026-05-18T10:00:00Z");
    private static final Instant START = NOW.plus(Duration.ofMinutes(10));
    private static final Instant END = NOW.plus(Duration.ofHours(2));

    @Nested
    @DisplayName("점검 윈도우 생성")
    class Create {

        @Test
        void FULL_점검_윈도우를_생성한다() {
            MaintenanceWindow window = MaintenanceWindow.of(
                MaintenanceScope.FULL,
                null,
                START,
                END,
                "정기 점검",
                "메시지",
                1L,
                NOW
            );

            assertThat(window.getScope()).isEqualTo(MaintenanceScope.FULL);
            assertThat(window.getTargetDomains()).isEmpty();
            assertThat(window.getStartAt()).isEqualTo(START);
            assertThat(window.getEndAt()).isEqualTo(END);
        }

        @Test
        void PER_DOMAIN_점검은_대상_도메인이_없으면_예외() {
            assertThatThrownBy(() -> MaintenanceWindow.of(
                MaintenanceScope.PER_DOMAIN,
                Set.of(),
                START,
                END,
                "도메인 점검",
                "메시지",
                1L,
                NOW
            )).isInstanceOf(MaintenanceDomainException.class)
                .hasFieldOrPropertyWithValue("baseCode", MaintenanceErrorCode.TARGET_DOMAINS_REQUIRED);
        }

        @Test
        void 종료_시각이_시작보다_빠르면_예외() {
            assertThatThrownBy(() -> MaintenanceWindow.of(
                MaintenanceScope.FULL,
                null,
                END,
                START,
                "잘못된 점검",
                "메시지",
                1L,
                NOW
            )).isInstanceOf(MaintenanceDomainException.class)
                .hasFieldOrPropertyWithValue("baseCode", MaintenanceErrorCode.INVALID_TIME_RANGE);
        }

        @Test
        void 시작_시각이_과거_60초보다_더_이전이면_예외() {
            Instant tooOld = NOW.minus(Duration.ofMinutes(5));
            assertThatThrownBy(() -> MaintenanceWindow.of(
                MaintenanceScope.FULL,
                null,
                tooOld,
                END,
                "잘못된 점검",
                "메시지",
                1L,
                NOW
            )).isInstanceOf(MaintenanceDomainException.class)
                .hasFieldOrPropertyWithValue("baseCode", MaintenanceErrorCode.START_AT_IN_PAST);
        }

        @Test
        void 시작_시각이_과거_60초_이내면_허용된다() {
            Instant graceWithin = NOW.minusSeconds(30);
            MaintenanceWindow window = MaintenanceWindow.of(
                MaintenanceScope.FULL,
                null,
                graceWithin,
                END,
                "즉시 점검",
                "메시지",
                1L,
                NOW
            );
            assertThat(window.getStartAt()).isEqualTo(graceWithin);
        }
    }

    @Nested
    @DisplayName("활성/예약 상태 판정")
    class StatusJudgement {

        @Test
        void 시작_이전이면_upcoming() {
            MaintenanceWindow window = MaintenanceWindow.of(
                MaintenanceScope.FULL, null, START, END, "t", "m", 1L, NOW
            );

            assertThat(window.isActiveAt(NOW)).isFalse();
            assertThat(window.isUpcomingAt(NOW)).isTrue();
        }

        @Test
        void 시작과_종료_사이는_active() {
            MaintenanceWindow window = MaintenanceWindow.of(
                MaintenanceScope.FULL, null, START, END, "t", "m", 1L, NOW
            );
            Instant middle = START.plus(Duration.ofMinutes(30));

            assertThat(window.isActiveAt(middle)).isTrue();
            assertThat(window.isUpcomingAt(middle)).isFalse();
        }

        @Test
        void 종료_시각_이후는_비활성() {
            MaintenanceWindow window = MaintenanceWindow.of(
                MaintenanceScope.FULL, null, START, END, "t", "m", 1L, NOW
            );
            Instant after = END.plus(Duration.ofMinutes(1));

            assertThat(window.isActiveAt(after)).isFalse();
            assertThat(window.isUpcomingAt(after)).isFalse();
        }

        @Test
        void 강제_종료된_윈도우는_active_도_upcoming_도_아님() {
            MaintenanceWindow window = MaintenanceWindow.of(
                MaintenanceScope.FULL, null, START, END, "t", "m", 1L, NOW
            );
            Instant middle = START.plus(Duration.ofMinutes(30));
            window.forceEnd(middle, 99L);

            assertThat(window.isActiveAt(middle)).isFalse();
            assertThat(window.isUpcomingAt(middle)).isFalse();
            assertThat(window.getForcedEndedAt()).isEqualTo(middle);
            assertThat(window.getForcedEndedBy()).isEqualTo(99L);
        }
    }

    @Nested
    @DisplayName("강제 종료")
    class ForceEnd {

        @Test
        void 이미_종료된_윈도우는_강제종료_불가() {
            MaintenanceWindow window = MaintenanceWindow.of(
                MaintenanceScope.FULL, null, START, END, "t", "m", 1L, NOW
            );
            window.forceEnd(START.plus(Duration.ofMinutes(30)), 99L);

            assertThatThrownBy(() -> window.forceEnd(START.plus(Duration.ofMinutes(45)), 99L))
                .isInstanceOf(MaintenanceDomainException.class)
                .hasFieldOrPropertyWithValue("baseCode", MaintenanceErrorCode.ALREADY_ENDED);
        }

        @Test
        void 자연_종료된_윈도우_강제종료_시도시_예외() {
            MaintenanceWindow window = MaintenanceWindow.of(
                MaintenanceScope.FULL, null, START, END, "t", "m", 1L, NOW
            );

            assertThatThrownBy(() -> window.forceEnd(END.plus(Duration.ofMinutes(5)), 99L))
                .isInstanceOf(MaintenanceDomainException.class)
                .hasFieldOrPropertyWithValue("baseCode", MaintenanceErrorCode.ALREADY_ENDED);
        }

        @Test
        void 강제_종료_시_요청자_memberId_가_기록된다() {
            MaintenanceWindow window = MaintenanceWindow.of(
                MaintenanceScope.FULL, null, START, END, "t", "m", 1L, NOW
            );
            Instant middle = START.plus(Duration.ofMinutes(15));

            window.forceEnd(middle, 42L);

            assertThat(window.getForcedEndedAt()).isEqualTo(middle);
            assertThat(window.getForcedEndedBy()).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("URI 차단 판정")
    class BlocksUri {

        @Test
        void FULL_점검은_모든_URI_차단() {
            MaintenanceWindow window = MaintenanceWindow.of(
                MaintenanceScope.FULL, null, START, END, "t", "m", 1L, NOW
            );

            assertThat(window.blocks("/api/v1/projects/1")).isTrue();
            assertThat(window.blocks("/api/v1/notices/2")).isTrue();
            assertThat(window.blocks("/api/v1/random-thing")).isTrue();
        }

        @Test
        void PER_DOMAIN_점검은_대상_도메인만_차단() {
            MaintenanceWindow window = MaintenanceWindow.of(
                MaintenanceScope.PER_DOMAIN,
                EnumSet.of(MaintenanceDomain.CHALLENGER),
                START, END, "t", "m", 1L, NOW
            );

            assertThat(window.blocks("/api/v1/challenger/me")).isTrue();
            assertThat(window.blocks("/api/v1/challenger-record/1")).isTrue();
            assertThat(window.blocks("/api/v1/notices/2")).isFalse();
            assertThat(window.blocks("/api/v1/projects/3")).isFalse();
        }
    }
}
