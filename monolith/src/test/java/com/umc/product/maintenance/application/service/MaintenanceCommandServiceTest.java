package com.umc.product.maintenance.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.umc.product.maintenance.application.port.in.command.dto.StartMaintenanceCommand;
import com.umc.product.maintenance.application.port.out.LoadMaintenanceWindowPort;
import com.umc.product.maintenance.application.port.out.SaveMaintenanceWindowPort;
import com.umc.product.maintenance.domain.MaintenanceScope;
import com.umc.product.maintenance.domain.MaintenanceWindow;
import com.umc.product.maintenance.exception.MaintenanceDomainException;
import com.umc.product.maintenance.exception.MaintenanceErrorCode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaintenanceCommandServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-18T10:00:00Z");
    private static final Instant START = NOW.plus(Duration.ofMinutes(10));
    private static final Instant END = NOW.plus(Duration.ofHours(2));

    @Mock
    LoadMaintenanceWindowPort loadPort;

    @Mock
    SaveMaintenanceWindowPort savePort;

    @Mock
    MaintenanceStateHolder stateHolder;

    MaintenanceCommandService sut;

    @BeforeEach
    void setUp() {
        sut = new MaintenanceCommandService(loadPort, savePort, stateHolder, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Nested
    @DisplayName("점검 시작")
    class Start {

        @Test
        void 겹치는_윈도우가_없으면_저장하고_캐시_갱신() {
            given(loadPort.findOverlapping(START, END)).willReturn(List.of());
            MaintenanceWindow saved = MaintenanceWindow.of(
                MaintenanceScope.FULL, null, START, END, "t", "m", 1L, NOW
            );
            given(savePort.save(any(MaintenanceWindow.class))).willReturn(saved);

            sut.start(new StartMaintenanceCommand(
                MaintenanceScope.FULL, null, START, END, "t", "m", 1L
            ));

            verify(savePort).save(any(MaintenanceWindow.class));
            verify(stateHolder).refresh();
        }

        @Test
        void 겹치는_윈도우가_있으면_예외() {
            MaintenanceWindow existing = MaintenanceWindow.of(
                MaintenanceScope.FULL, null, START, END, "t", "m", 1L, NOW
            );
            given(loadPort.findOverlapping(START, END)).willReturn(List.of(existing));

            assertThatThrownBy(() -> sut.start(new StartMaintenanceCommand(
                MaintenanceScope.FULL, null, START, END, "t", "m", 2L
            ))).isInstanceOf(MaintenanceDomainException.class)
                .hasFieldOrPropertyWithValue("baseCode", MaintenanceErrorCode.OVERLAPPING_WINDOW);

            verify(savePort, never()).save(any());
            verify(stateHolder, never()).refresh();
        }
    }

    @Nested
    @DisplayName("강제 종료")
    class ForceEnd {

        @Test
        void 존재하지_않는_id_면_예외() {
            given(loadPort.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> sut.forceEnd(999L, 1L))
                .isInstanceOf(MaintenanceDomainException.class)
                .hasFieldOrPropertyWithValue("baseCode", MaintenanceErrorCode.MAINTENANCE_WINDOW_NOT_FOUND);
        }

        @Test
        void 활성_윈도우를_강제_종료하고_캐시_갱신() {
            MaintenanceWindow window = MaintenanceWindow.of(
                MaintenanceScope.FULL, null, START, END, "t", "m", 1L, NOW
            );
            given(loadPort.findById(7L)).willReturn(Optional.of(window));
            when(savePort.save(window)).thenReturn(window);

            // NOW 는 START 이전이라 forceEnd 호출 시 endAt 이후가 아닌 활성 상태 직전.
            // 실제 시계는 START 이후로 만들어야 forceEnd 가능 → 다른 clock 주입.
            Instant duringMaintenance = START.plus(Duration.ofMinutes(30));
            MaintenanceCommandService duringSut = new MaintenanceCommandService(
                loadPort, savePort, stateHolder, Clock.fixed(duringMaintenance, ZoneOffset.UTC)
            );

            duringSut.forceEnd(7L, 1L);

            assertThat(window.getForcedEndedAt()).isEqualTo(duringMaintenance);
            verify(stateHolder).refresh();
        }
    }
}
